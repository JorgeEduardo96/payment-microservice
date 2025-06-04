package br.com.clientservice.domain.messaging.consumer;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.messaging.producer.ClientProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ClientProducer underTest;

    @Test
    void sendClientEvent() throws Exception {
        var client = mock(ClientOutputDTO.class);
        var topic = "topic-test";
        var json = "{\"id\":\"1\"}";

        when(objectMapper.writeValueAsString(client)).thenReturn(json);

        underTest.sendClientEvent(topic, client);

        verify(kafkaTemplate).send(topic, json);
    }

    @Test
    void sendClientEvent_shouldThrowExceptionWhenSerializingFails() throws Exception {
        var client = mock(ClientOutputDTO.class);
        var topic = "topic-test";

        when(objectMapper.writeValueAsString(client)).thenThrow(new JsonProcessingException("Failed to serialize") {
        });

        RuntimeException ex = assertThrows(RuntimeException.class, () -> underTest.sendClientEvent(topic, client));
        assertTrue(ex.getMessage().contains("Failed to serialize"));
    }
}
