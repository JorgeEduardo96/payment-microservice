package br.com.notificationservice.domain.repository;

import br.com.notificationservice.domain.dto.ClientEventDTO;

import java.util.UUID;

public interface ClientRepository {

    void upsert(ClientEventDTO clientEventDTO);

    ClientEventDTO findById(UUID id);

}
