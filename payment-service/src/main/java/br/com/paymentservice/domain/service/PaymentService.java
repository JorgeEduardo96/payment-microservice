package br.com.paymentservice.domain.service;

import br.com.paymentservice.domain.dto.OutboxInputDTO;
import br.com.paymentservice.domain.dto.PaymentCreateInputDTO;
import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import br.com.paymentservice.domain.event.OutboxCreatedEvent;
import br.com.paymentservice.domain.repository.OutboxRepository;
import br.com.paymentservice.domain.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void processPayment(String orderId, String clientId, String paymentMethod) {
        var orderUuid = UUID.fromString(orderId);

        if (paymentRepository.findByOrderId(orderUuid) != null) {
            log.info("Payment for orderId {} was already processed, skipping duplicate request", orderId);
            return;
        }

        var status = isLastCharacterLetter(orderId) ? "PAID" : "FAILED";

        PaymentResponseDTO persisted;
        try {
            persisted = paymentRepository.insert(new PaymentCreateInputDTO(
                    orderUuid, UUID.fromString(clientId), paymentMethod, status));
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent duplicate payment detected for orderId {}, skipping", orderId);
            return;
        }

        log.info("Payment persisted for orderId {}, status: {}", orderId, status);

        var outboxId = outboxRepository.insert(new OutboxInputDTO(
                "Payment",
                orderId,
                "payment-processed",
                objectMapper.valueToTree(persisted).toString()
        ));

        publisher.publishEvent(new OutboxCreatedEvent(outboxId));
    }

    private boolean isLastCharacterLetter(String id) {
        char lastChar = id.charAt(id.length() - 1);
        return Character.isLetter(lastChar);
    }
}
