package br.com.clientservice.domain.service;

import br.com.clientservice.domain.dto.ClientInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.event.ClientCreatedEvent;
import br.com.clientservice.domain.repository.ClientRepository;
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

    public void insert(ClientInputDTO inputDTO) {
        var persisted = repository.insert(inputDTO);
        log.info("New client persisted, id: {}", persisted.id());

        publisher.publishEvent(new ClientCreatedEvent(persisted));
    }

    public ClientOutputDTO findClient(UUID id) {
        return repository.findById(id);
    }


}
