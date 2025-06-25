package br.com.notificationservice.messaging.consumer;

import br.com.notificationservice.domain.dto.PaymentResponseEventDTO;
import br.com.notificationservice.domain.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentConsumer underTest;

    @Test
    void consume() throws JsonProcessingException {
        var message = "{\"orderId\":\"123e4567-e89b-12d3-a456-426614174000\",\"status\":\"PAID\"}";
        var mockedPaymentResponse = mock(PaymentResponseEventDTO.class);

        when(objectMapper.readValue(message, PaymentResponseEventDTO.class))
                .thenReturn(mockedPaymentResponse);
        when(mockedPaymentResponse.orderId()).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

        underTest.consume(message);

        verify(objectMapper).readValue(message, PaymentResponseEventDTO.class);
        verify(notificationService).sendNotification(mockedPaymentResponse);
    }

}
