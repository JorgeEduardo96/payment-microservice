package br.com.orderservice.messaging;

import br.com.orderservice.domain.entity.OrderSagaJpaEntity;
import br.com.orderservice.domain.enumeration.SagaStatus;
import br.com.orderservice.domain.repository.OrderSagaRepository;
import br.com.orderservice.domain.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSagaTimeoutJobTest {

    @Mock
    private OrderSagaRepository sagaRepository;
    @Mock
    private OrderService orderService;

    private OrderSagaTimeoutJob underTest;

    @BeforeEach
    void setUp() {
        underTest = new OrderSagaTimeoutJob(sagaRepository, orderService);
    }

    @Test
    void doesNothingWhenNoSagasAreStuck() {
        when(sagaRepository.findStuckInPaymentRequested(any())).thenReturn(List.of());

        underTest.compensateStuckSagas();

        verifyNoInteractions(orderService);
    }

    @Test
    void cancelsOrderAndMarksSagaCompensatedWhenStuck() {
        UUID orderId = UUID.randomUUID();
        OrderSagaJpaEntity stuckSaga = stuckSagaFor(orderId);

        when(sagaRepository.findStuckInPaymentRequested(any())).thenReturn(List.of(stuckSaga));

        underTest.compensateStuckSagas();

        verify(orderService).cancelOrder(orderId);
        ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(sagaRepository).markCompensated(org.mockito.ArgumentMatchers.eq(orderId), reasonCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(reasonCaptor.getValue()).contains("Timed out");
        verify(sagaRepository, never()).registerCompensationFailure(any(), anyString());
    }

    @Test
    void registersCompensationFailureWhenCancelOrderThrows() {
        UUID orderId = UUID.randomUUID();
        OrderSagaJpaEntity stuckSaga = stuckSagaFor(orderId);

        when(sagaRepository.findStuckInPaymentRequested(any())).thenReturn(List.of(stuckSaga));
        doThrow(new RuntimeException("DB unavailable")).when(orderService).cancelOrder(orderId);

        underTest.compensateStuckSagas();

        verify(sagaRepository).registerCompensationFailure(org.mockito.ArgumentMatchers.eq(orderId), anyString());
        verify(sagaRepository, never()).markCompensated(any(), anyString());
    }

    private OrderSagaJpaEntity stuckSagaFor(UUID orderId) {
        OrderSagaJpaEntity saga = new OrderSagaJpaEntity();
        saga.setId(UUID.randomUUID());
        saga.setOrderId(orderId);
        saga.setStatus(SagaStatus.PAYMENT_REQUESTED);
        saga.setRetryCount(0);
        saga.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        saga.setUpdatedAt(LocalDateTime.now().minusMinutes(10));
        return saga;
    }
}
