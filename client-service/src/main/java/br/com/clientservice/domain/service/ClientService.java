package br.com.clientservice.domain.service;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;
import br.com.clientservice.domain.dto.OutboxInputDTO;
import br.com.clientservice.domain.event.OutboxCreatedEvent;
import br.com.clientservice.domain.repository.ClientRepository;
import br.com.clientservice.domain.repository.OutboxRepository;
import br.com.sharedlib.model.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository repository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher publisher;
    private final KeycloakUserProvisioningService keycloakUserProvisioningService;

    @Transactional
    public ClientOutputDTO insert(ClientCreateInputDTO inputDTO) {
        var persisted = repository.insert(inputDTO);
        log.info("New client persisted, id: {}", persisted.id());
        String userId = keycloakUserProvisioningService.createUser(persisted.name(), persisted.email(), persisted.id());
        log.info("New client persisted at keycloak, id: {}", userId);

        try {
            keycloakUserProvisioningService.assignClientRole(userId);

            var outboxId = outboxRepository.insert(new OutboxInputDTO(
                    "Client",
                    persisted.id().toString(),
                    "client-created",
                    objectMapper.valueToTree(persisted).toString()
            ));

            publisher.publishEvent(new OutboxCreatedEvent(outboxId));
        } catch (Exception e) {
            keycloakUserProvisioningService.deleteUser(UUID.fromString(userId));
            throw new BusinessException("Failed to complete client registration: " + e.getMessage(), e);
        }

        return persisted;
    }

    @Transactional
    public ClientOutputDTO update(UUID id, ClientUpdateInputDTO inputDTO) {
        var clientWithSameEmail = repository.findByEmail(inputDTO.email());
        if (clientWithSameEmail != null && !clientWithSameEmail.id().equals(id)) {
            throw new BusinessException("Email already exists");
        }

        var updatedClient = repository.update(id, inputDTO);
        log.info("Client updated, id: {}", updatedClient.id());

        var outboxId = outboxRepository.insert(new OutboxInputDTO(
                "Client",
                updatedClient.id().toString(),
                "client-updated",
                objectMapper.valueToTree(updatedClient).toString()
        ));

        publisher.publishEvent(new OutboxCreatedEvent(outboxId));

        return updatedClient;
    }

    public ClientOutputDTO findClient(UUID id) {
        return repository.findById(id);
    }

    public List<ClientOutputDTO> findAll() {
        return repository.findAll();
    }


}
