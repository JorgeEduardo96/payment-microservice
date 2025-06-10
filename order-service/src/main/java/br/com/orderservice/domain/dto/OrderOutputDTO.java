package br.com.orderservice.domain.dto;

import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.enumeration.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderOutputDTO(UUID id, BigDecimal total, String shippingAddress, UUID clientId, String clientName,
                             OrderStatus status, PaymentMethod paymentMethod) {
}
