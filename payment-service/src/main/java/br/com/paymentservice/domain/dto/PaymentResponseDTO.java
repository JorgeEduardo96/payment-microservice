package br.com.paymentservice.domain.dto;

import java.util.UUID;

public record PaymentResponseDTO(UUID orderId, String status, UUID clientId) {
}
