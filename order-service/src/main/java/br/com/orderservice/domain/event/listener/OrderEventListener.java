package br.com.orderservice.domain.event.listener;

import br.com.orderservice.domain.event.OrderCreatedEvent;
import br.com.orderservice.grpc.client.PaymentGrpcClient;
import br.com.orderservice.grpc.client.stub.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final PaymentGrpcClient paymentGrpcClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        var outputDTO = event.orderOutputDTO();
        log.info("AFTER COMMIT: Processing payment for order: {}", event.orderOutputDTO().id());
        paymentGrpcClient.processPayment(PaymentRequest.newBuilder()
                .setOrderId(outputDTO.id().toString())
                .setAmount(outputDTO.total().doubleValue())
                .setPaymentMethod(outputDTO.paymentMethod().getDescription())
                .build());
        log.info("Payment process sent for order: {}", event.orderOutputDTO().id());
    }

}
