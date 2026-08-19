package br.com.paymentservice.messaging;

import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import br.com.paymentservice.domain.entity.OutboxJpaEntity;
import br.com.paymentservice.domain.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    public static final String PAYMENT_TOPIC = "payment-topic";

    private static final Duration DEFAULT_GRACE_PERIOD = Duration.ofSeconds(30);

    private final PaymentProducer paymentProducer;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publish(UUID outboxId) {
        OutboxJpaEntity outbox = outboxRepository.findById(outboxId);
        try {
            doPublish(outbox);
            outboxRepository.markPublished(outbox.getId());
        } catch (Exception e) {
            log.error("Failed to send outbox event {}: {}", outbox.getId(), e.getMessage());
            outboxRepository.markFailed(outbox.getId(), e.getMessage());
        }
    }

    public void publishRetryableBatch() {
        publishRetryableBatch(DEFAULT_GRACE_PERIOD);
    }

    public void publishRetryableBatch(Duration gracePeriod) {
        LocalDateTime olderThan = LocalDateTime.now().minus(gracePeriod);
        List<OutboxJpaEntity> outboxes = outboxRepository.findRetryableBatch(olderThan);

        for (OutboxJpaEntity outbox : outboxes) {
            try {
                outboxRepository.markProcessing(outbox.getId());
                doPublish(outboxRepository.findById(outbox.getId()));
                outboxRepository.markPublished(outbox.getId());
            } catch (Exception e) {
                log.error("Failed to republish outbox {}: {}", outbox.getId(), e.getMessage());
                outboxRepository.markFailed(outbox.getId(), e.getMessage());
            }
        }
    }

    private void doPublish(OutboxJpaEntity outbox) throws Exception {
        PaymentResponseDTO payment = objectMapper.readValue(outbox.getPayload(), PaymentResponseDTO.class);
        log.info("Sending kafka event for outbox: {}, topic: {}", outbox.getId(), PAYMENT_TOPIC);
        paymentProducer.sendPaymentEvent(PAYMENT_TOPIC, payment);
    }
}
