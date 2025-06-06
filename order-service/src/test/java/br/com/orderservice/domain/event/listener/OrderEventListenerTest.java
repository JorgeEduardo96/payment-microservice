package br.com.orderservice.domain.event.listener;

import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.enumeration.PaymentMethod;
import br.com.orderservice.domain.event.OrderCreatedEvent;
import br.com.orderservice.grpc.client.PaymentGrpcClient;
import br.com.orderservice.grpc.client.stub.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderEventListenerTest {

    @Mock
    private PaymentGrpcClient paymentGrpcClient;

    @InjectMocks
    private OrderEventListener underTest;

    @Test
    void handleOrderCreated() {
        UUID orderId = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("250.75");
        PaymentMethod paymentMethod = PaymentMethod.CARD;

        OrderOutputDTO orderOutputDTO = mock(OrderOutputDTO.class);
        when(orderOutputDTO.id()).thenReturn(orderId);
        when(orderOutputDTO.total()).thenReturn(totalAmount);
        when(orderOutputDTO.paymentMethod()).thenReturn(paymentMethod);

        OrderCreatedEvent event = new OrderCreatedEvent(orderOutputDTO);

        ArgumentCaptor<PaymentRequest> paymentRequestCaptor = ArgumentCaptor.forClass(PaymentRequest.class);

        underTest.handleOrderCreated(event);

        verify(paymentGrpcClient).processPayment(paymentRequestCaptor.capture());

        PaymentRequest capturedRequest = paymentRequestCaptor.getValue();

        assertThat(capturedRequest).isNotNull();
        assertThat(capturedRequest.getOrderId()).isEqualTo(orderId.toString());
        assertThat(capturedRequest.getAmount()).isEqualTo(totalAmount.doubleValue());
        assertThat(capturedRequest.getPaymentMethod()).isEqualTo(paymentMethod.getDescription());

        verifyNoMoreInteractions(paymentGrpcClient);
    }
}
