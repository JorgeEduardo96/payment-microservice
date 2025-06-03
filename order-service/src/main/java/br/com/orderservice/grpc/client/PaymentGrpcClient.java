package br.com.orderservice.grpc.client;

import br.com.orderservice.grpc.client.stub.PaymentRequest;
import br.com.orderservice.grpc.client.stub.PaymentServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class PaymentGrpcClient {

    @GrpcClient("payment-service")
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;

    public void processPayment(PaymentRequest paymentRequest) {
        paymentStub.processPayment(paymentRequest);
    }
}
