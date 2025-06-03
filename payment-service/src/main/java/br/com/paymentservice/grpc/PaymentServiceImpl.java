package br.com.paymentservice.grpc;

import br.com.orderservice.grpc.client.stub.PaymentRequest;
import br.com.orderservice.grpc.client.stub.PaymentServiceGrpc;
import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import br.com.paymentservice.messaging.producer.PaymentProducer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;
import java.util.logging.Logger;

@GrpcService
public class PaymentServiceImpl extends PaymentServiceGrpc.PaymentServiceImplBase {

    private static final Logger logger = Logger.getLogger(PaymentServiceImpl.class.getName());

    private final PaymentProducer paymentProducer;

    public PaymentServiceImpl(PaymentProducer paymentProducer) {
        this.paymentProducer = paymentProducer;
    }

    @Override
    public void processPayment(PaymentRequest request, StreamObserver<Empty> responseObserver) {
        var orderId = request.getOrderId();
        var status = isLastCharacterLetter(orderId) ? "PAID" : "FAILED";

        logger.info(String.format("Processing payment for orderId: %s", orderId));
        paymentProducer.sendPaymentEvent("payment-topic", new PaymentResponseDTO(UUID.fromString(orderId), status));

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private boolean isLastCharacterLetter(String id) {
        char lastChar = id.charAt(id.length() - 1);
        return Character.isLetter(lastChar);
    }
}
