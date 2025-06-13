package br.com.clientservice.messaging.producer;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

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
        String topic = "test-topic";
        String payload = "{\"id\":\"1\"}";

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, payload);
        RecordMetadata metadata = mock(RecordMetadata.class);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);

        when(objectMapper.writeValueAsString(client)).thenReturn(payload);
        when(kafkaTemplate.send(topic, payload)).thenReturn(CompletableFuture.completedFuture(sendResult));

        underTest.sendClientEvent(topic, client);

        verify(kafkaTemplate).send(topic, payload);
        verify(objectMapper).writeValueAsString(client);
    }
}
