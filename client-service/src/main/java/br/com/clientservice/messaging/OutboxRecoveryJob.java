package br.com.clientservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRecoveryJob {

    private final OutboxPublisher outboxPublisher;

    @Scheduled(fixedDelayString = "${client-service.outbox.retry-delay-ms:43200000}")
    public void recoverPendingOutbox() {
        log.debug("Running outbox recovery job");
        outboxPublisher.publishRetryableBatch();
    }
}
