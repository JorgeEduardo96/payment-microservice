package br.com.paymentservice.domain.event.listener;

import br.com.paymentservice.domain.event.OutboxCreatedEvent;
import br.com.paymentservice.messaging.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final OutboxPublisher outboxPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxCreated(OutboxCreatedEvent event) {
        outboxPublisher.publish(event.outboxId());
    }
}
