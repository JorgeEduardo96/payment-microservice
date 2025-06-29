package br.com.orderservice.messaging;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.PaymentResponseEventDTO;
import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.enumeration.PaymentMethod;
import br.com.orderservice.domain.repository.ClientRepository;
import br.com.orderservice.domain.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"payment-topic", "payment-topic.DLQ"})
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PaymentConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private KafkaProducer<String, String> producer;

    private UUID orderId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producer = new KafkaProducer<>(producerProps, new StringSerializer(), new JsonSerializer<>());

        clientId = UUID.randomUUID();

        clientRepository.upsert(new ClientEventDTO(clientId, "John Doe", LocalDateTime.now(), null));
        var order = orderRepository.createOrder(new OrderInputDTO(clientId, new BigDecimal("100.00"), "123 Main St", PaymentMethod.CARD, null));
        orderId = order.id();
    }

    @Test
    void shouldConsumePaymentResponseEventAndUpdateOrderStatus() throws Exception {
        var paymentResponseEvent = new PaymentResponseEventDTO(orderId, "PAID", "CARD", clientId);
        var message = objectMapper.writeValueAsString(paymentResponseEvent);

        producer.send(new ProducerRecord<>("payment-topic", message));
        producer.flush();

        Thread.sleep(5000);
        var updatedOrder = orderRepository.ordersByClientId(clientId).stream()
                .filter(order -> order.id().equals(orderId))
                .findFirst()
                .orElse(null);
        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldSendMessageToDLQAfterRetries() throws Exception {
        var invalidMessage = "{ \"orderId\": null, \"paymentMethod\": invalid-payload";

        producer.send(new ProducerRecord<>("payment-topic", invalidMessage));
        producer.flush();

        Thread.sleep(5000);

        var consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafka);
        try (var consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer())) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "payment-topic.DLQ");
            var records = KafkaTestUtils.getRecords(consumer);

            assertThat(records.count()).isGreaterThan(0);

            var record = records.iterator().next();
            assertThat(record.value()).contains("invalid-payload");
        }
    }
}
