package br.com.paymentservice.grpc;

import br.com.orderservice.grpc.client.stub.PaymentRequest;
import br.com.paymentservice.domain.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentServiceImpl underTest;

    @Test
    void processPaymentDelegatesToPaymentService() {
        var paymentRequest = mock(PaymentRequest.class);
        var orderId = UUID.randomUUID().toString();
        var clientId = UUID.randomUUID().toString();

        when(paymentRequest.getOrderId()).thenReturn(orderId);
        when(paymentRequest.getPaymentMethod()).thenReturn("CARD");
        when(paymentRequest.getClientId()).thenReturn(clientId);

        underTest.processPayment(paymentRequest, mock(io.grpc.stub.StreamObserver.class));

        verify(paymentService).processPayment(orderId, clientId, "CARD");
    }

    @Test
    void processPaymentSwallowsExceptionsFromPaymentService() {
        var paymentRequest = mock(PaymentRequest.class);
        var orderId = UUID.randomUUID().toString();
        var clientId = UUID.randomUUID().toString();
        var responseObserver = mock(io.grpc.stub.StreamObserver.class);

        when(paymentRequest.getOrderId()).thenReturn(orderId);
        when(paymentRequest.getPaymentMethod()).thenReturn("CARD");
        when(paymentRequest.getClientId()).thenReturn(clientId);
        doThrow(new RuntimeException("boom")).when(paymentService).processPayment(orderId, clientId, "CARD");

        underTest.processPayment(paymentRequest, responseObserver);

        // The gRPC contract must still complete successfully even if persisting/publishing the
        // payment failed — the caller (order-service) isn't meant to see internal failures here.
        verify(responseObserver).onNext(com.google.protobuf.Empty.getDefaultInstance());
        verify(responseObserver).onCompleted();
    }

}

