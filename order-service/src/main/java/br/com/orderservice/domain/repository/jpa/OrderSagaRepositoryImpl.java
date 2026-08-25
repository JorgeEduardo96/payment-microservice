package br.com.orderservice.domain.repository.jpa;

import br.com.orderservice.domain.entity.OrderSagaJpaEntity;
import br.com.orderservice.domain.enumeration.SagaStatus;
import br.com.orderservice.domain.repository.OrderSagaRepository;
import br.com.orderservice.domain.repository.jpa.crudrepository.OrderSagaJpaEntityCrudRepository;
import br.com.sharedlib.model.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderSagaRepositoryImpl implements OrderSagaRepository {

    private final OrderSagaJpaEntityCrudRepository crudRepository;

    @Value("${order-service.saga.max-compensation-retries:3}")
    private int maxCompensationRetries;

    @Override
    public void create(UUID orderId) {
        var entity = new OrderSagaJpaEntity();
        entity.setOrderId(orderId);
        entity.setStatus(SagaStatus.STARTED);
        crudRepository.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPaymentRequested(UUID orderId) {
        updateStatus(orderId, SagaStatus.PAYMENT_REQUESTED, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(UUID orderId) {
        updateStatus(orderId, SagaStatus.COMPLETED, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompensated(UUID orderId, String reason) {
        updateStatus(orderId, SagaStatus.COMPENSATED, reason);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerCompensationFailure(UUID orderId, String reason) {
        var saga = findByOrderIdOrThrow(orderId);
        saga.setRetryCount(saga.getRetryCount() + 1);
        saga.setLastError(reason);
        if (saga.getRetryCount() >= maxCompensationRetries) {
            saga.setStatus(SagaStatus.FAILED);
        }
        crudRepository.save(saga);
    }

    @Override
    public List<OrderSagaJpaEntity> findStuckInPaymentRequested(LocalDateTime olderThan) {
        return crudRepository.findByStatusAndUpdatedAtBefore(SagaStatus.PAYMENT_REQUESTED, olderThan);
    }

    private void updateStatus(UUID orderId, SagaStatus status, String lastError) {
        var saga = findByOrderIdOrThrow(orderId);
        saga.setStatus(status);
        if (lastError != null) {
            saga.setLastError(lastError);
        }
        crudRepository.save(saga);
    }

    private OrderSagaJpaEntity findByOrderIdOrThrow(UUID orderId) {
        return crudRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("OrderSaga", orderId.toString()));
    }
}
