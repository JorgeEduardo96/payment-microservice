package br.com.clientservice.domain.service;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;
import br.com.clientservice.domain.event.ClientCreatedEvent;
import br.com.clientservice.domain.event.ClientUpdatedEvent;
import br.com.clientservice.domain.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository repository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public ClientOutputDTO insert(ClientCreateInputDTO inputDTO) {
        var persisted = repository.insert(inputDTO);
        log.info("New client persisted, id: {}", persisted.id());

        publisher.publishEvent(new ClientCreatedEvent(persisted));

        return persisted;
    }

    @Transactional
    public ClientOutputDTO update(UUID id, ClientUpdateInputDTO inputDTO) {
        var updatedClient = repository.update(id, inputDTO);
        log.info("Client updated, id: {}", updatedClient.id());

        publisher.publishEvent(new ClientUpdatedEvent(updatedClient));

        return updatedClient;
    }

    public ClientOutputDTO findClient(UUID id) {
        return repository.findById(id);
    }


}
