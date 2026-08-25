package br.com.orderservice.domain.repository.jpa;

import br.com.orderservice.domain.entity.OrderSagaJpaEntity;
import br.com.orderservice.domain.enumeration.SagaStatus;
import br.com.orderservice.domain.repository.jpa.crudrepository.OrderSagaJpaEntityCrudRepository;
import br.com.sharedlib.model.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSagaRepositoryImplTest {

    @Mock
    private OrderSagaJpaEntityCrudRepository crudRepository;

    private OrderSagaRepositoryImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new OrderSagaRepositoryImpl(crudRepository);
        ReflectionTestUtils.setField(underTest, "maxCompensationRetries", 3);
    }

    @Test
    void createPersistsSagaInStartedStatus() {
        UUID orderId = UUID.randomUUID();

        underTest.create(orderId);

        ArgumentCaptor<OrderSagaJpaEntity> captor = ArgumentCaptor.forClass(OrderSagaJpaEntity.class);
        verify(crudRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(captor.getValue().getStatus()).isEqualTo(SagaStatus.STARTED);
    }

    @Test
    void markPaymentRequestedUpdatesExistingSaga() {
        UUID orderId = UUID.randomUUID();
        OrderSagaJpaEntity saga = sagaFor(orderId, SagaStatus.STARTED);
        when(crudRepository.findByOrderId(orderId)).thenReturn(Optional.of(saga));

        underTest.markPaymentRequested(orderId);

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.PAYMENT_REQUESTED);
        verify(crudRepository).save(saga);
    }

    @Test
    void markCompletedUpdatesExistingSaga() {
        UUID orderId = UUID.randomUUID();
        OrderSagaJpaEntity saga = sagaFor(orderId, SagaStatus.PAYMENT_REQUESTED);
        when(crudRepository.findByOrderId(orderId)).thenReturn(Optional.of(saga));

        underTest.markCompleted(orderId);

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPLETED);
        verify(crudRepository).save(saga);
    }

    @Test
    void markCompensatedUpdatesStatusAndReason() {
        UUID orderId = UUID.randomUUID();
        OrderSagaJpaEntity saga = sagaFor(orderId, SagaStatus.PAYMENT_REQUESTED);
        when(crudRepository.findByOrderId(orderId)).thenReturn(Optional.of(saga));

        underTest.markCompensated(orderId, "payment declined");

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
        assertThat(saga.getLastError()).isEqualTo("payment declined");
        verify(crudRepository).save(saga);
    }

    @Test
    void updateThrowsWhenSagaNotFound() {
        UUID orderId = UUID.randomUUID();
        when(crudRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> underTest.markCompleted(orderId));
    }

    @Test
    void registerCompensationFailureIncrementsRetryCountWithoutReachingMax() {
        UUID orderId = UUID.randomUUID();
        OrderSagaJpaEntity saga = sagaFor(orderId, SagaStatus.PAYMENT_REQUESTED);
        saga.setRetryCount(0);
        when(crudRepository.findByOrderId(orderId)).thenReturn(Optional.of(saga));

        underTest.registerCompensationFailure(orderId, "db down");

        assertThat(saga.getRetryCount()).isEqualTo(1);
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.PAYMENT_REQUESTED);
        assertThat(saga.getLastError()).isEqualTo("db down");
    }

    @Test
    void registerCompensationFailureMarksSagaFailedWhenMaxRetriesReached() {
        UUID orderId = UUID.randomUUID();
        OrderSagaJpaEntity saga = sagaFor(orderId, SagaStatus.PAYMENT_REQUESTED);
        saga.setRetryCount(2);
        when(crudRepository.findByOrderId(orderId)).thenReturn(Optional.of(saga));

        underTest.registerCompensationFailure(orderId, "db down again");

        assertThat(saga.getRetryCount()).isEqualTo(3);
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.FAILED);
    }

    @Test
    void findStuckInPaymentRequestedDelegatesToCrudRepository() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        OrderSagaJpaEntity saga = sagaFor(UUID.randomUUID(), SagaStatus.PAYMENT_REQUESTED);
        when(crudRepository.findByStatusAndUpdatedAtBefore(SagaStatus.PAYMENT_REQUESTED, threshold))
                .thenReturn(List.of(saga));

        List<OrderSagaJpaEntity> result = underTest.findStuckInPaymentRequested(threshold);

        assertThat(result).containsExactly(saga);
        verify(crudRepository).findByStatusAndUpdatedAtBefore(SagaStatus.PAYMENT_REQUESTED, threshold);
        verifyNoMoreInteractions(crudRepository);
    }

    private OrderSagaJpaEntity sagaFor(UUID orderId, SagaStatus status) {
        OrderSagaJpaEntity saga = new OrderSagaJpaEntity();
        saga.setId(UUID.randomUUID());
        saga.setOrderId(orderId);
        saga.setStatus(status);
        saga.setRetryCount(0);
        return saga;
    }
}
