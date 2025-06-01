package br.com.orderservice.domain.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public enum PaymentMethod {

    CARD("Card") {
        @Override
        public BigDecimal appliedDiscount() {
            return BigDecimal.ZERO;
        }
    },
    CASH("Cash") {
        @Override
        public BigDecimal appliedDiscount() {
            return BigDecimal.TEN;
        }
    };

    private final String description;

    public abstract BigDecimal appliedDiscount();


}
