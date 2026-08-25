package br.com.orderservice.messaging;

import br.com.orderservice.domain.entity.OrderSagaJpaEntity;
import br.com.orderservice.domain.repository.OrderSagaRepository;
import br.com.orderservice.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaTimeoutJob {

    private final OrderSagaRepository sagaRepository;
    private final OrderService orderService;

    @Value("${order-service.saga.timeout-minutes:5}")
    private long timeoutMinutes;

    @Scheduled(fixedDelayString = "${order-service.saga.check-interval-ms:60000}")
    public void compensateStuckSagas() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<OrderSagaJpaEntity> stuckSagas = sagaRepository.findStuckInPaymentRequested(threshold);

        if (stuckSagas.isEmpty()) {
            return;
        }

        log.warn("Found {} saga(s) stuck in PAYMENT_REQUESTED for more than {} minute(s)",
                stuckSagas.size(), timeoutMinutes);

        for (OrderSagaJpaEntity saga : stuckSagas) {
            compensate(saga);
        }
    }

    private void compensate(OrderSagaJpaEntity saga) {
        String reason = "Timed out waiting for payment confirmation after " + timeoutMinutes + " minute(s)";
        try {
            orderService.cancelOrder(saga.getOrderId());
            sagaRepository.markCompensated(saga.getOrderId(), reason);
            log.warn("Saga {} for order {} timed out and was compensated (order cancelled)",
                    saga.getId(), saga.getOrderId());
        } catch (Exception e) {
            String failureReason = reason + " - compensation attempt failed: " + e.getMessage();
            sagaRepository.registerCompensationFailure(saga.getOrderId(), failureReason);
            log.error("Failed to compensate saga {} for order {}: {}",
                    saga.getId(), saga.getOrderId(), e.getMessage());
        }
    }
}
