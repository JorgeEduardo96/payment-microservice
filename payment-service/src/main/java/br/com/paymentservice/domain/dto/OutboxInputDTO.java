package br.com.paymentservice.domain.dto;

public record OutboxInputDTO(String aggregateType,
                              String aggregateId,
                              String eventType,
                              String payload) {
}
