package br.com.orderservice.grpc.server;

import br.com.orderservice.grpc.client.stub.PaymentRequest;
import br.com.orderservice.grpc.client.stub.PaymentServiceGrpc;
import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

public class InProcessServer {
    public static final String SERVER_NAME = "test-server";
    public static boolean called = false;

    private Server server;

    public void start() throws Exception {
        server = InProcessServerBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .addService(new PaymentServiceGrpc.PaymentServiceImplBase() {
                    @Override
                    public void processPayment(PaymentRequest request, StreamObserver<Empty> responseObserver) {
                        called = true;
                        responseObserver.onNext(Empty.getDefaultInstance());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();
    }

    public void shutdown() {
        if (server != null) server.shutdownNow();
    }
}
