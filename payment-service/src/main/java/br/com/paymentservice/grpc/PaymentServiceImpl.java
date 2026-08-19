package br.com.paymentservice.grpc;

import br.com.orderservice.grpc.client.stub.PaymentRequest;
import br.com.orderservice.grpc.client.stub.PaymentServiceGrpc;
import br.com.paymentservice.domain.service.PaymentService;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class PaymentServiceImpl extends PaymentServiceGrpc.PaymentServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentService paymentService;

    public PaymentServiceImpl(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public void processPayment(PaymentRequest request, StreamObserver<Empty> responseObserver) {
        var orderId = request.getOrderId();
        logger.info("Processing payment for orderId: {}", orderId);

        try {
            paymentService.processPayment(orderId, request.getClientId(), request.getPaymentMethod());
        } catch (Exception e) {
            logger.warn("Failed to process payment for orderId {}: {}", orderId, e.getMessage());
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}

