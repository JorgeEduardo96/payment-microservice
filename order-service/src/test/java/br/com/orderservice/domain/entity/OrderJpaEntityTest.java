package br.com.orderservice.domain.entity;

import br.com.orderservice.domain.enumeration.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderJpaEntityTest {

    @Test
    void applyDiscount() {
        OrderJpaEntity order = new OrderJpaEntity();
        order.setTotal(BigDecimal.valueOf(100.00));
        order.setPaymentMethod(PaymentMethod.CARD);

        order.applyDiscount();

        BigDecimal expectedTotal = new BigDecimal("100.00");
        assertEquals(expectedTotal.toString(), order.getTotal().toString());

        OrderJpaEntity order2 = new OrderJpaEntity();
        order2.setTotal(BigDecimal.valueOf(100.00));
        order2.setPaymentMethod(PaymentMethod.CASH);

        order2.applyDiscount();

        BigDecimal expectedTotal2 = new BigDecimal("90.00");
        assertEquals(expectedTotal2.toString(), order2.getTotal().toString());
    }
}
