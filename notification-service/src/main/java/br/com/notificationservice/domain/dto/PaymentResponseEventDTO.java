package br.com.notificationservice.domain.dto;

import java.util.UUID;

public record PaymentResponseEventDTO(UUID orderId, String status) {

}
