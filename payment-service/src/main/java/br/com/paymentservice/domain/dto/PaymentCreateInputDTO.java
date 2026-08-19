package br.com.paymentservice.domain.dto;

import java.util.UUID;

public record PaymentCreateInputDTO(UUID orderId, UUID clientId, String paymentMethod, String status) {
}
