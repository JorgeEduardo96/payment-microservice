package br.com.notificationservice.domain.repository.jpa;

import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.exception.EntityNotFoundException;
import br.com.notificationservice.domain.mapper.ClientMapper;
import br.com.notificationservice.domain.repository.ClientRepository;
import br.com.notificationservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientRepositoryImpl implements ClientRepository {

    private final ClientMapper mapper;
    private final ClientJpaEntityCrudRepository repository;

    @Override
    public void upsert(ClientEventDTO clientEventDTO) {
        log.info("Upserting client: {}", clientEventDTO);
        var clientEntity = repository.findById(clientEventDTO.id())
                .map(existingClient -> {
                    existingClient.setName(clientEventDTO.name());
                    existingClient.setEmail(clientEventDTO.email());
                    return existingClient;
                })
                .orElseGet(() -> mapper.toEntity(clientEventDTO));

        repository.save(clientEntity);
    }

    @Override
    public ClientEventDTO findById(UUID id) {
        return repository.findById(id).map(mapper::toDto).orElseThrow(() -> new EntityNotFoundException("Client", id));
    }
}
