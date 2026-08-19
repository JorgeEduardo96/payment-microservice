package br.com.paymentservice.domain.service;

import br.com.paymentservice.domain.dto.PaymentCreateInputDTO;
import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import br.com.paymentservice.domain.event.OutboxCreatedEvent;
import br.com.paymentservice.domain.repository.OutboxRepository;
import br.com.paymentservice.domain.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private ApplicationEventPublisher publisher;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PaymentService underTest;

    @Test
    void persistsPaymentAndPublishesOutboxEventWhenLastCharacterIsLetter() {
        var orderId = UUID.randomUUID().toString().replaceFirst(".$", "a");
        var clientId = UUID.randomUUID().toString();
        var persisted = new PaymentResponseDTO(UUID.fromString(orderId), "PAID", "CARD", UUID.fromString(clientId));
        var outboxId = UUID.randomUUID();

        when(paymentRepository.findByOrderId(UUID.fromString(orderId))).thenReturn(null);
        when(paymentRepository.insert(any(PaymentCreateInputDTO.class))).thenReturn(persisted);
        when(outboxRepository.insert(any())).thenReturn(outboxId);

        underTest.processPayment(orderId, clientId, "CARD");

        ArgumentCaptor<PaymentCreateInputDTO> captor = ArgumentCaptor.forClass(PaymentCreateInputDTO.class);
        verify(paymentRepository).insert(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo("PAID");

        verify(outboxRepository).insert(any());
        verify(publisher).publishEvent(any(OutboxCreatedEvent.class));
    }

    @Test
    void decidesFailedStatusWhenLastCharacterIsDigit() {
        var orderId = UUID.randomUUID().toString().replaceFirst(".$", "1");
        var clientId = UUID.randomUUID().toString();

        when(paymentRepository.findByOrderId(UUID.fromString(orderId))).thenReturn(null);
        when(paymentRepository.insert(any(PaymentCreateInputDTO.class)))
                .thenAnswer(invocation -> {
                    PaymentCreateInputDTO dto = invocation.getArgument(0);
                    return new PaymentResponseDTO(dto.orderId(), dto.status(), dto.paymentMethod(), dto.clientId());
                });
        when(outboxRepository.insert(any())).thenReturn(UUID.randomUUID());

        underTest.processPayment(orderId, clientId, "CASH");

        ArgumentCaptor<PaymentCreateInputDTO> captor = ArgumentCaptor.forClass(PaymentCreateInputDTO.class);
        verify(paymentRepository).insert(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo("FAILED");
    }

    @Test
    void skipsReprocessingWhenPaymentForOrderAlreadyExists() {
        var orderId = UUID.randomUUID().toString();
        var clientId = UUID.randomUUID().toString();
        var existing = new PaymentResponseDTO(UUID.fromString(orderId), "PAID", "CARD", UUID.fromString(clientId));

        when(paymentRepository.findByOrderId(UUID.fromString(orderId))).thenReturn(existing);

        underTest.processPayment(orderId, clientId, "CARD");

        verify(paymentRepository, never()).insert(any());
        verify(outboxRepository, never()).insert(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void skipsPublishingWhenConcurrentInsertViolatesUniqueOrderIdConstraint() {
        var orderId = UUID.randomUUID().toString();
        var clientId = UUID.randomUUID().toString();

        when(paymentRepository.findByOrderId(UUID.fromString(orderId))).thenReturn(null);
        when(paymentRepository.insert(any(PaymentCreateInputDTO.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        underTest.processPayment(orderId, clientId, "CARD");

        verify(outboxRepository, never()).insert(any());
        verify(publisher, never()).publishEvent(any());
    }
}
