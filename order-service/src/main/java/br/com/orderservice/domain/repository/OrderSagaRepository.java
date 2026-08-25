package br.com.orderservice.domain.repository;

import br.com.orderservice.domain.entity.OrderSagaJpaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderSagaRepository {

    void create(UUID orderId);

    void markPaymentRequested(UUID orderId);

    void markCompleted(UUID orderId);

    void markCompensated(UUID orderId, String reason);

    // Called when a compensation attempt itself fails (e.g. DB unavailable). Increments
    // the retry counter and, once the configured max is reached, transitions the saga to
    // the terminal FAILED status so the timeout job stops retrying it forever.
    void registerCompensationFailure(UUID orderId, String reason);

    List<OrderSagaJpaEntity> findStuckInPaymentRequested(LocalDateTime olderThan);
}
