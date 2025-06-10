package br.com.orderservice.domain.dto;

import java.util.UUID;

public record PaymentResponseEventDTO(UUID orderId, String status, UUID clientId) {

}
