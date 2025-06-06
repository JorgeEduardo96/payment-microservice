package br.com.orderservice.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "client")
@Data
public class ClientJpaEntity {

    @Id
    private UUID id;

    @Column(unique = true)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "client")
    private List<OrderJpaEntity> orders;
}
