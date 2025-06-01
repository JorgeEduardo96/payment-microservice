package br.com.orderservice.domain.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {

    PENDING_PAYMENT("Pending Payment"),
    PAID("Paid");

    private final String description;

}
