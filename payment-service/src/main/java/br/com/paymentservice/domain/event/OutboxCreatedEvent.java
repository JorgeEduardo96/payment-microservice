package br.com.paymentservice.domain.event;

import java.util.UUID;

public record OutboxCreatedEvent(UUID outboxId) {
}
