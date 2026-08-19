package br.com.clientservice.domain.event;

import java.util.UUID;

public record OutboxCreatedEvent(UUID outboxId) {
}
