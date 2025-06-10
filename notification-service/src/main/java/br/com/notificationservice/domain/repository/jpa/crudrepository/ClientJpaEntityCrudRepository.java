package br.com.notificationservice.domain.repository.jpa.crudrepository;

import br.com.notificationservice.domain.entity.ClientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientJpaEntityCrudRepository extends JpaRepository<ClientJpaEntity, UUID> {
}
