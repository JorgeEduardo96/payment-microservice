package br.com.orderservice.domain.repository;

import br.com.orderservice.domain.dto.ClientEventDTO;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {

    Optional<ClientEventDTO> findById(UUID id);

    void upsert(ClientEventDTO dto);

}
