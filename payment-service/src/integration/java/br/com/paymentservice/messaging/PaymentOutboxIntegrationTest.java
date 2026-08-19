package br.com.paymentservice.messaging;

import br.com.paymentservice.domain.entity.OutboxJpaEntity;
import br.com.paymentservice.domain.entity.PaymentJpaEntity;
import br.com.paymentservice.domain.repository.jpa.crudrepository.OutboxJpaEntityCrudRepository;
import br.com.paymentservice.domain.repository.jpa.crudrepository.PaymentJpaEntityCrudRepository;
import br.com.paymentservice.domain.service.PaymentService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// grpc.server.port=-1 disables the embedded gRPC server (see PaymentProducerIntegrationTest for
// why: payment-service's context otherwise auto-starts a real gRPC server on the fixed port 9090,
// which races with PaymentServiceGrpcIntegrationTest's own context when both run in the same JVM).
// This test exercises the PaymentService layer directly, so gRPC isn't needed here anyway.
//
// @DirtiesContext forces a fresh Spring context (and therefore a fresh embedded Kafka broker) for
// this class: without it, Spring may reuse the same cached context/broker across this class and
// PaymentProducerIntegrationTest (both share identical @SpringBootTest properties), and leftover
// messages on "payment-topic" from one class would be replayed into the other's consumers (Kafka
// consumers default to auto.offset.reset=earliest for brand-new consumer groups).
@SpringBootTest(properties = "grpc.server.port=-1")
@EmbeddedKafka(
        partitions = 1,
        topics = {"payment-topic"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@EnableKafka
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentOutboxIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private PaymentJpaEntityCrudRepository paymentCrudRepository;

    @Autowired
    private OutboxJpaEntityCrudRepository outboxCrudRepository;

    @AfterEach
    void cleanUp() {
        outboxCrudRepository.deleteAll();
        paymentCrudRepository.deleteAll();
    }

    @Test
    void shouldPersistPaymentAndOutboxAndPublishPaidEventWhenLastCharacterIsLetter() throws Exception {
        var topic = "payment-topic";
        var orderId = UUID.randomUUID().toString().replaceFirst(".$", "a");
        var clientId = UUID.randomUUID().toString();
        var listener = startListener(topic);

        try {
            paymentService.processPayment(orderId, clientId, "CARD");

            ConsumerRecord<String, String> received = listener.records.poll(10, TimeUnit.SECONDS);
            assertThat(received).isNotNull();
            assertThat(received.topic()).isEqualTo(topic);
            assertThat(received.value()).contains(orderId, "PAID", "CARD", clientId);

            var payment = awaitPaymentByOrderId(orderId);
            assertThat(payment).isNotNull();
            assertThat(payment.getOrderId()).isEqualTo(UUID.fromString(orderId));
            assertThat(payment.getClientId()).isEqualTo(UUID.fromString(clientId));
            assertThat(payment.getPaymentMethod()).isEqualTo("CARD");
            assertThat(payment.getStatus()).isEqualTo("PAID");

            var outbox = awaitOutboxByAggregateId(orderId);
            assertThat(outbox.getAggregateType()).isEqualTo("Payment");
            assertThat(outbox.getAggregateId()).isEqualTo(orderId);
            assertThat(outbox.getEventType()).isEqualTo("payment-processed");
            assertThat(outbox.getStatus()).isEqualTo("PUBLISHED");
            assertThat(outbox.getPayload()).contains(orderId, "PAID", "CARD", clientId);
        } finally {
            listener.container.stop();
        }
    }

    @Test
    void shouldPersistPaymentAndOutboxAndPublishFailedEventWhenLastCharacterIsDigit() throws Exception {
        var topic = "payment-topic";
        var orderId = UUID.randomUUID().toString().replaceFirst(".$", "1");
        var clientId = UUID.randomUUID().toString();
        var listener = startListener(topic);

        try {
            paymentService.processPayment(orderId, clientId, "CASH");

            ConsumerRecord<String, String> received = listener.records.poll(10, TimeUnit.SECONDS);
            assertThat(received).isNotNull();
            assertThat(received.value()).contains(orderId, "FAILED", "CASH", clientId);

            var payment = awaitPaymentByOrderId(orderId);
            assertThat(payment.getStatus()).isEqualTo("FAILED");

            var outbox = awaitOutboxByAggregateId(orderId);
            assertThat(outbox.getStatus()).isEqualTo("PUBLISHED");
        } finally {
            listener.container.stop();
        }
    }

    @Test
    void shouldNotDuplicatePaymentOrOutboxWhenSameOrderIdIsProcessedTwice() throws Exception {
        var topic = "payment-topic";
        var orderId = UUID.randomUUID().toString().replaceFirst(".$", "a");
        var clientId = UUID.randomUUID().toString();
        var listener = startListener(topic);

        try {
            paymentService.processPayment(orderId, clientId, "CARD");
            assertThat(listener.records.poll(10, TimeUnit.SECONDS)).isNotNull();
            awaitOutboxByAggregateId(orderId);

            // Simulates order-service retrying the gRPC call for an already-processed order
            // (e.g. after a timeout): idempotency must kick in and no reprocessing should happen.
            paymentService.processPayment(orderId, clientId, "CARD");

            assertThat(listener.records.poll(2, TimeUnit.SECONDS)).isNull();
            // Scoped to this test's own orderId/aggregateId rather than the full table, since the
            // "payment"/"outbox" tables live in a single shared H2 in-memory database for the
            // whole JVM (see application-integration.yml) and may already contain rows written by
            // other integration test classes running in the same test run.
            assertThat(paymentCrudRepository.findByOrderId(UUID.fromString(orderId))).isPresent();
            assertThat(outboxCrudRepository.findByAggregateId(orderId)).hasSize(1);
        } finally {
            listener.container.stop();
        }
    }

    private ListenerHandle startListener(String topic) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "payment-outbox-integration-" + topic + "-" + UUID.randomUUID(), "false", embeddedKafka);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        var consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        var container = new KafkaMessageListenerContainer<>(consumerFactory, new ContainerProperties(topic));
        var records = new LinkedBlockingQueue<ConsumerRecord<String, String>>();

        container.setupMessageListener((MessageListener<String, String>) records::offer);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());

        return new ListenerHandle(container, records);
    }

    private PaymentJpaEntity awaitPaymentByOrderId(String orderId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10000L;
        while (System.currentTimeMillis() < deadline) {
            var payment = paymentCrudRepository.findByOrderId(UUID.fromString(orderId)).orElse(null);
            if (payment != null) {
                return payment;
            }
            Thread.sleep(100L);
        }
        return paymentCrudRepository.findByOrderId(UUID.fromString(orderId)).orElse(null);
    }

    private OutboxJpaEntity awaitOutboxByAggregateId(String aggregateId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10000L;
        while (System.currentTimeMillis() < deadline) {
            var outboxes = outboxCrudRepository.findByAggregateId(aggregateId);
            if (!outboxes.isEmpty() && "PUBLISHED".equals(outboxes.get(0).getStatus())) {
                return outboxes.get(0);
            }
            Thread.sleep(100L);
        }
        return outboxCrudRepository.findByAggregateId(aggregateId).get(0);
    }

    private record ListenerHandle(
            KafkaMessageListenerContainer<String, String> container,
            LinkedBlockingQueue<ConsumerRecord<String, String>> records
    ) {
    }
}
