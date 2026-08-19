package br.com.paymentservice.domain.repository.jpa.crudrepository;

import br.com.paymentservice.domain.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentJpaEntityCrudRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByOrderId(UUID orderId);
}
