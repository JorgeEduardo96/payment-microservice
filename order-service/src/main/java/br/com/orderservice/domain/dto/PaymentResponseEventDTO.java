package br.com.orderservice.domain.dto;

import java.util.UUID;

public record PaymentResponseEventDTO(UUID orderId, String status, String paymentMethod, UUID clientId) {

}
