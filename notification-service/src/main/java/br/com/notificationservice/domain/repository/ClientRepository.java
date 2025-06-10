package br.com.notificationservice.domain.repository;

import br.com.notificationservice.domain.dto.ClientEventDTO;

public interface ClientRepository {
    
    void upsertClient(ClientEventDTO clientEventDTO);

}
