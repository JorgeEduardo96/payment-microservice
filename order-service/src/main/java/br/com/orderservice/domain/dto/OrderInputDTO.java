package br.com.orderservice.domain.dto;

import br.com.orderservice.domain.enumeration.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderInputDTO(@NotNull UUID clientId, @NotNull BigDecimal total, @NotNull String shippingAddress,
                            @NotNull PaymentMethod paymentMethod, String notes) {
}
