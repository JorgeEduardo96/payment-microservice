package br.com.notificationservice.consumer;

import br.com.notificationservice.config.SendNotification;
import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.dto.PaymentResponseEventDTO;
import br.com.notificationservice.domain.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"payment-topic", "payment-topic.DLQ"})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("integration")
public class PaymentConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ClientRepository clientRepository;

    @MockitoBean
    private SendNotification sendNotification;

    private KafkaProducer<String, String> producer;

    private UUID orderId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producer = new KafkaProducer<>(producerProps, new StringSerializer(), new JsonSerializer<>());

        clientId = UUID.randomUUID();

        var email = String.format("john.doe.%s@email.com", RandomStringUtils.randomAlphabetic(3));

        clientRepository.upsert(new ClientEventDTO(clientId, "John Doe", email, LocalDateTime.now(), null));
        orderId = UUID.randomUUID();
    }

    @Test
    void shouldConsumePaymentEventAndSendNotificationForPaidOrders() throws Exception {
        var paymentResponseEvent = new PaymentResponseEventDTO(orderId, "PAID", "CARD", clientId);
        var message = objectMapper.writeValueAsString(paymentResponseEvent);

        producer.send(new ProducerRecord<>("payment-topic", message));
        producer.flush();

        Thread.sleep(5000);
        verify(sendNotification).send(any(SendNotification.Message.class));
    }

    @Test
    void shouldConsumePaymentEventAndNotSendNotificationForNotPaidOrders() throws Exception {
        var paymentResponseEvent = new PaymentResponseEventDTO(orderId, "FAILED", "CARD", clientId);
        var message = objectMapper.writeValueAsString(paymentResponseEvent);

        producer.send(new ProducerRecord<>("payment-topic", message));
        producer.flush();

        Thread.sleep(5000);
        verify(sendNotification, never()).send(any(SendNotification.Message.class));
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
