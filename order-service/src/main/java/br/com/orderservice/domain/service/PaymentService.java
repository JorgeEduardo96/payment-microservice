package br.com.orderservice.domain.service;

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

    @Retry(name = "defaultGrpcClientRetry", fallbackMethod = "fallbackProcessPayment")
    public void processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment for order: {}", paymentRequest.getOrderId());
        paymentGrpcClient.processPayment(paymentRequest);
        log.info("Payment processed for order: {}", paymentRequest.getOrderId());
    }

    @SuppressWarnings("unused")
    public void fallbackProcessPayment(PaymentRequest paymentRequest, Throwable throwable) {
        log.error("Failed to process payment for order: {}. Falling back due to: {}",
                paymentRequest.getOrderId(), throwable.getMessage());

        orderService.cancelOrder(UUID.fromString(paymentRequest.getOrderId()));
    }

}
