package br.com.orderservice.messaging.consumer;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.repository.ClientRepository;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"client-created-topic", "client-created-topic.DLQ"})
@ExtendWith(SpringExtension.class)
public class ClientConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    private ClientRepository repository;
    @Autowired
    private ObjectMapper objectMapper;

    private KafkaProducer<String, String> producer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producer = new KafkaProducer<>(producerProps, new StringSerializer(), new JsonSerializer<>());
    }

    @Test
    void shouldConsumeClientCreatedEventAndPersistInDatabase() throws Exception {
        var clientId = UUID.randomUUID();
        var event = new ClientEventDTO(clientId, "John Doe", LocalDateTime.now(), null);
        var message = objectMapper.writeValueAsString(event);

        producer.send(new ProducerRecord<>("client-created-topic", message));
        producer.flush();

        Thread.sleep(5000);

        var persistedClient = repository.findById(clientId).orElse(null);
        assertThat(persistedClient).isNotNull();
        assertThat(persistedClient.name()).isEqualTo("John Doe");
    }

    @Test
    void shouldSendMessageToDLQAfterRetries() throws Exception {
        var invalidMessage = "{ \"clientId\": null, \"name\": invalid-payload";

        producer.send(new ProducerRecord<>("client-created-topic", invalidMessage));
        producer.flush();

        Thread.sleep(5000);

        var consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafka);
        try (var consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer())) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "client-created-topic.DLQ");
            var records = KafkaTestUtils.getRecords(consumer);

            assertThat(records.count()).isGreaterThan(0);

            var record = records.iterator().next();
            assertThat(record.value()).contains("invalid-payload");
        }
    }
}
