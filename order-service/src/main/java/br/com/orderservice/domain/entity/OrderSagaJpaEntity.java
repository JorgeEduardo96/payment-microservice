package br.com.orderservice.domain.entity;

import br.com.orderservice.domain.enumeration.SagaStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_saga")
@Data
public class OrderSagaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(length = 500)
    private String lastError;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Refreshed on every status change - the saga timeout job relies on this to
    // find sagas that have been sitting in PAYMENT_REQUESTED for too long.
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
