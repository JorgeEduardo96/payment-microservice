package br.com.orderservice.messaging.consumer;

import br.com.orderservice.domain.dto.PaymentResponseEventDTO;
import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentConsumer underTest;

    @Test
    void consume() throws JsonProcessingException {
        var message = "{\"orderId\":\"123e4567-e89b-12d3-a456-426614174000\",\"status\":\"PAID\", \"clientId\":\"123e4567-e89b-12d3-a456-426614174001\"}";
        var paymentResponseEventDTO = new PaymentResponseEventDTO(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "PAID",
                "CARD",
                UUID.fromString("123e4567-e89b-12d3-a456-426614174001")
        );

        when(objectMapper.readValue(message, PaymentResponseEventDTO.class))
                .thenReturn(paymentResponseEventDTO);

        underTest.consume(message);

        verify(objectMapper).readValue(message, PaymentResponseEventDTO.class);
        verify(orderRepository).processPayment(
                paymentResponseEventDTO.orderId(),
                OrderStatus.valueOf(paymentResponseEventDTO.status().toUpperCase())
        );
    }

}
