package br.com.paymentservice.grpc;

import br.com.orderservice.grpc.client.stub.PaymentRequest;
import br.com.orderservice.grpc.client.stub.PaymentServiceGrpc;
import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import br.com.paymentservice.messaging.PaymentProducer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class PaymentServiceImpl extends PaymentServiceGrpc.PaymentServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentProducer paymentProducer;

    public PaymentServiceImpl(PaymentProducer paymentProducer) {
        this.paymentProducer = paymentProducer;
    }

    @Override
    public void processPayment(PaymentRequest request, StreamObserver<Empty> responseObserver) {
        var orderId = request.getOrderId();
        var status = isLastCharacterLetter(orderId) ? "PAID" : "FAILED";
        var paymentMethod = request.getPaymentMethod();
        var clientId = request.getClientId();

        logger.info("Processing payment for orderId: {}", orderId);
        try {
            paymentProducer.sendPaymentEvent("payment-topic", new PaymentResponseDTO(UUID.fromString(orderId),
                    status, paymentMethod, UUID.fromString(clientId)));
        } catch (Exception e) {
            logger.warn("Failed to send payment event: {}", e.getMessage());
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private boolean isLastCharacterLetter(String id) {
        char lastChar = id.charAt(id.length() - 1);
        return Character.isLetter(lastChar);
    }
}
