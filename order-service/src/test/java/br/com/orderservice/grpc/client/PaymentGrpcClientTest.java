package br.com.orderservice.grpc.client;

import br.com.orderservice.grpc.client.stub.PaymentServiceGrpc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentGrpcClientTest {

    @Mock
    private PaymentServiceGrpc.PaymentServiceBlockingStub stub;

    @InjectMocks
    private PaymentGrpcClient underTest;

    @Test
    void processPayment() {
        var paymentRequest = mock(br.com.orderservice.grpc.client.stub.PaymentRequest.class);

        underTest.processPayment(paymentRequest);

        verify(stub).processPayment(paymentRequest);
    }

}
