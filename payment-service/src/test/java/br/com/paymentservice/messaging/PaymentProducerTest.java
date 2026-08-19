package br.com.paymentservice.messaging;

import br.com.paymentservice.domain.dto.PaymentResponseDTO;
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

@ExtendWith({MockitoExtension.class})
public class PaymentProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentProducer underTest;

    @Test
    void sendPaymentEvent() throws Exception {
        var mockedResponseDto = mock(PaymentResponseDTO.class);
        String topic = "test-topic";

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, mockedResponseDto);
        RecordMetadata metadata = mock(RecordMetadata.class);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);

        when(kafkaTemplate.send(topic, mockedResponseDto)).thenReturn(CompletableFuture.completedFuture(sendResult));

        underTest.sendPaymentEvent(topic, mockedResponseDto);

        // The DTO must be sent directly (not pre-serialized) so the configured JsonSerializer
        // is the only place serialization happens — avoiding double-encoding of the payload.
        verify(kafkaTemplate).send(topic, mockedResponseDto);
    }


}
