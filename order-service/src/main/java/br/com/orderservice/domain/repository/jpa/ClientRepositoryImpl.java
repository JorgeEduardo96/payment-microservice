package br.com.orderservice.domain.repository.jpa;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.repository.ClientRepository;
import br.com.orderservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import br.com.orderservice.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientRepositoryImpl implements ClientRepository {

    private final ClientMapper mapper;
    private final ClientJpaEntityCrudRepository repository;

    @Override
    public Optional<ClientEventDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toDTO);
    }

    @Override
    public void upsert(ClientEventDTO clientEventDTO) {
        log.info("Upserting client: {}", clientEventDTO);
        var clientEntity = repository.findById(clientEventDTO.id())
                .map(existingClient -> {
                    existingClient.setName(clientEventDTO.name());
                    return existingClient;
                })
                .orElseGet(() -> mapper.toEntity(clientEventDTO));

        repository.save(clientEntity);
    }

}
