package br.com.orderservice.domain.dto;

import br.com.orderservice.domain.enumeration.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderOutputDTO(UUID id, BigDecimal total, String shippingAddress, String clientName, OrderStatus status) {
}
