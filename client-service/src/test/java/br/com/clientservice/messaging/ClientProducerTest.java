package br.com.clientservice.messaging;

import br.com.clientservice.domain.dto.ClientOutputDTO;
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

    @InjectMocks
    private ClientProducer underTest;

    @Test
    void sendClientEvent() throws Exception {
        var client = mock(ClientOutputDTO.class);
        String topic = "test-topic";

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, client);
        RecordMetadata metadata = mock(RecordMetadata.class);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);

        when(kafkaTemplate.send(topic, client)).thenReturn(CompletableFuture.completedFuture(sendResult));

        underTest.sendClientEvent(topic, client);

        verify(kafkaTemplate).send(topic, client);
    }
}
