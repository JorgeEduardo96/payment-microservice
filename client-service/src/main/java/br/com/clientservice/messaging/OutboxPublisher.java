package br.com.clientservice.messaging;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.entity.OutboxJpaEntity;
import br.com.clientservice.domain.repository.OutboxRepository;
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

    public static final String CLIENT_CREATED_TOPIC = "client-created-topic";
    public static final String CLIENT_UPDATED_TOPIC = "client-updated-topic";

    private static final Duration DEFAULT_GRACE_PERIOD = Duration.ofSeconds(30);

    private final ClientProducer producer;
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
        String topic = resolveTopic(outbox.getEventType());
        ClientOutputDTO client = objectMapper.readValue(outbox.getPayload(), ClientOutputDTO.class);
        log.info("Sending kafka event for outbox: {}, topic: {}", outbox.getId(), topic);
        producer.sendClientEvent(topic, client);
    }

    private String resolveTopic(String eventType) {
        if ("client-created".equals(eventType)) {
            return CLIENT_CREATED_TOPIC;
        }
        if ("client-updated".equals(eventType)) {
            return CLIENT_UPDATED_TOPIC;
        }
        throw new IllegalArgumentException("Unsupported outbox event type: " + eventType);
    }
}
