package br.com.clientservice.messaging;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.messaging.producer.ClientProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"client-created-topic", "client-updated-topic"})
@EnableKafka
public class ClientProducerFallbackIntegrationTest {

    @MockitoSpyBean
    private ClientProducer clientProducer;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void shouldTriggerFallbackWhenKafkaFails() throws Exception {
        var dto = new ClientOutputDTO(UUID.randomUUID(), "John", "john.doe@email.com", "98765432100", LocalDateTime.now(), null);
        String topic = "client-created-topic";

        when(kafkaTemplate.send(any(String.class), any(Object.class)))
                .thenThrow(new RuntimeException("Kafka unavailable"));

        clientProducer.sendClientEvent(topic, dto);

        verify(clientProducer).fallback(eq(topic), eq(dto), any(Throwable.class));
    }
}
