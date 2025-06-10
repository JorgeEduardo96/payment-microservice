package br.com.orderservice.grpc.server;

import br.com.orderservice.grpc.client.stub.PaymentServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class StubConfig {

    @Bean
    public ManagedChannel inProcessChannel() {
        return InProcessChannelBuilder
                .forName(InProcessServer.SERVER_NAME)
                .directExecutor()
                .build();
    }

    @Bean
    public PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub(ManagedChannel channel) {
        return PaymentServiceGrpc.newBlockingStub(channel);
    }
}
