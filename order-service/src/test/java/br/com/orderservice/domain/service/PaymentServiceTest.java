package br.com.orderservice.domain.service;

import br.com.orderservice.domain.repository.OrderSagaRepository;
import br.com.orderservice.grpc.client.PaymentGrpcClient;
import br.com.orderservice.grpc.client.stub.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderService orderService;
    @Mock
    private PaymentGrpcClient paymentGrpcClient;
    @Mock
    private OrderSagaRepository sagaRepository;

    @InjectMocks
    private PaymentService underTest;

    @Test
    void processPaymentMarksSagaAsPaymentRequestedBeforeCallingGrpc() {
        UUID orderId = UUID.randomUUID();
        PaymentRequest request = PaymentRequest.newBuilder()
                .setOrderId(orderId.toString())
                .setAmount(100.0)
                .setPaymentMethod("Card")
                .setClientId(UUID.randomUUID().toString())
                .build();

        underTest.processPayment(request);

        verify(sagaRepository).markPaymentRequested(orderId);
        verify(paymentGrpcClient).processPayment(request);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void fallbackProcessPaymentCancelsOrderAndMarksSagaCompensated() {
        UUID orderId = UUID.randomUUID();
        PaymentRequest request = PaymentRequest.newBuilder()
                .setOrderId(orderId.toString())
                .setAmount(100.0)
                .setPaymentMethod("Card")
                .setClientId(UUID.randomUUID().toString())
                .build();

        underTest.fallbackProcessPayment(request, new RuntimeException("payment-service unavailable"));

        verify(orderService).cancelOrder(orderId);
        verify(sagaRepository).markCompensated(eq(orderId), contains("payment-service unavailable"));
    }
}
