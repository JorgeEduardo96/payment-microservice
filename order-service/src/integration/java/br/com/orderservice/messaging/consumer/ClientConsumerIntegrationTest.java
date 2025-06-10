package br.com.orderservice.messaging.consumer;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"client-created-topic"})
@ExtendWith(SpringExtension.class)
public class ClientConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    private ClientRepository repository;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private ClientConsumer clientConsumer;

    private KafkaProducer<String, String> producer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producer = new KafkaProducer<>(producerProps, new StringSerializer(), new JsonSerializer<>());
    }

    @Test
    void shouldConsumeClientCreatedEventAndPersistInDatabase() throws Exception {
        var clientId = UUID.randomUUID();
        var event = new ClientEventDTO(clientId, "Maria Teste", LocalDateTime.now(), null);
        var message = objectMapper.writeValueAsString(event);

        producer.send(new ProducerRecord<>("client-created-topic", message));
        producer.flush();

        Thread.sleep(2000);
        var persistedClient = repository.findById(clientId).orElse(null);
        assertThat(persistedClient).isNotNull();
        assertThat(persistedClient.name()).isEqualTo("Maria Teste");
    }

    @Test
    void shouldCallFallbackWhenConsumeFails() throws Exception {
        String badMessage = "invalid json";

        producer.send(new ProducerRecord<>("client-created-topic", badMessage));
        producer.flush();

        Thread.sleep(4000);

        verify(clientConsumer, atLeastOnce()).fallback(eq(badMessage), any(Exception.class));
    }
}
