package br.com.orderservice.domain.dto;

import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.enumeration.PaymentMethod;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderOutputDTO(UUID id, BigDecimal total, String shippingAddress, String clientName, OrderStatus status, PaymentMethod paymentMethod) {
}
