package br.com.paymentservice.messaging;

import br.com.paymentservice.domain.dto.PaymentResponseDTO;
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

@ExtendWith({MockitoExtension.class})
public class PaymentProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentProducer underTest;

    @Test
    void sendPaymentEvent() throws Exception {
        var mockedResponseDto = mock(PaymentResponseDTO.class);
        String topic = "test-topic";
        var payload = "mocked payload";

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, payload);
        RecordMetadata metadata = mock(RecordMetadata.class);
        SendResult<String, Object> sendResult = new SendResult<>(record, metadata);

        when(objectMapper.writeValueAsString(mockedResponseDto)).thenReturn(payload);
        when(kafkaTemplate.send(topic, payload)).thenReturn(CompletableFuture.completedFuture(sendResult));

        underTest.sendPaymentEvent(topic, mockedResponseDto);

        verify(kafkaTemplate).send(topic, payload);
        verify(objectMapper).writeValueAsString(mockedResponseDto);
    }


}
