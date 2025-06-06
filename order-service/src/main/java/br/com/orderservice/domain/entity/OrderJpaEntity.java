package br.com.orderservice.domain.entity;

import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.enumeration.PaymentMethod;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_tb")
@Data
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientJpaEntity client;

    public void applyDiscount() {
        BigDecimal percentual = paymentMethod.appliedDiscount().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal discountValue = this.total.multiply(percentual);
        this.total = this.total.subtract(discountValue).setScale(2, RoundingMode.HALF_UP);
    }
}
