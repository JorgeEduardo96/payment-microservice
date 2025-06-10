package br.com.orderservice.grpc.client;

import br.com.orderservice.grpc.client.stub.PaymentRequest;
import br.com.orderservice.grpc.server.InProcessServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "grpc.client.payment-service.address=in-process:test-server",
        "grpc.client.payment-service.negotiation-type=PLAINTEXT"
})
@SpringBootTest
public class PaymentGrpcClientIntegrationTest {

    private static InProcessServer inProcessServer;

    @Autowired
    private PaymentGrpcClient paymentGrpcClient;

    @BeforeAll
    static void setupServer() throws Exception {
        inProcessServer = new InProcessServer();
        inProcessServer.start();
    }

    @AfterAll
    static void stopServer() {
        inProcessServer.shutdown();
    }

    @Test
    void shouldCallPaymentServiceSuccessfully() {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setOrderId(UUID.randomUUID().toString())
                .setAmount(100.0)
                .build();

        paymentGrpcClient.processPayment(request);

        assertThat(InProcessServer.called).isTrue();
    }


}
