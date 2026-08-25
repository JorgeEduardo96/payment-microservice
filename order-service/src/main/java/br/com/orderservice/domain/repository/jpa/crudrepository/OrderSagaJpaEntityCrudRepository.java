package br.com.orderservice.domain.repository.jpa.crudrepository;

import br.com.orderservice.domain.entity.OrderSagaJpaEntity;
import br.com.orderservice.domain.enumeration.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderSagaJpaEntityCrudRepository extends JpaRepository<OrderSagaJpaEntity, UUID> {

    Optional<OrderSagaJpaEntity> findByOrderId(UUID orderId);

    List<OrderSagaJpaEntity> findByStatusAndUpdatedAtBefore(SagaStatus status, LocalDateTime updatedAt);
}
