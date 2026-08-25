package br.com.orderservice.domain.service;

import br.com.orderservice.domain.repository.OrderSagaRepository;
import br.com.orderservice.grpc.client.PaymentGrpcClient;
import br.com.orderservice.grpc.client.stub.PaymentRequest;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderService orderService;
    private final PaymentGrpcClient paymentGrpcClient;
    private final OrderSagaRepository sagaRepository;

    @Retry(name = "payment", fallbackMethod = "fallbackProcessPayment")
    public void processPayment(PaymentRequest paymentRequest) {
        UUID orderId = UUID.fromString(paymentRequest.getOrderId());
        log.info("Processing payment for order: {}", orderId);
        // Idempotent: also runs on every @Retry attempt, which conveniently refreshes
        // updatedAt so the saga timeout job doesn't treat an in-progress retry as stuck.
        sagaRepository.markPaymentRequested(orderId);
        paymentGrpcClient.processPayment(paymentRequest);
        log.info("Payment processed for order: {}", orderId);
    }

    @SuppressWarnings("unused")
    public void fallbackProcessPayment(PaymentRequest paymentRequest, Throwable throwable) {
        UUID orderId = UUID.fromString(paymentRequest.getOrderId());
        log.error("Failed to process payment for order: {}. Falling back due to: {}",
                orderId, throwable.getMessage());

        orderService.cancelOrder(orderId);
        sagaRepository.markCompensated(orderId, "gRPC payment call failed after retries: " + throwable.getMessage());
    }

}
