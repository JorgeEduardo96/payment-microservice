package br.com.clientservice.domain.repository.jpa.crudrepository;

import br.com.clientservice.domain.entity.ClientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientJpaEntityCrudRepository extends JpaRepository<ClientJpaEntity, UUID> {

    Optional<ClientJpaEntity> findByEmail(String email);
    Optional<ClientJpaEntity> findByCpf(String cpf);

}
