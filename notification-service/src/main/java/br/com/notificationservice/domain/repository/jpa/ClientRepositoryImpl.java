package br.com.notificationservice.domain.repository.jpa;

import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.mapper.ClientMapper;
import br.com.notificationservice.domain.repository.ClientRepository;
import br.com.notificationservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientRepositoryImpl implements ClientRepository {

    private final ClientMapper mapper;
    private final ClientJpaEntityCrudRepository repository;

    @Override
    public void upsertClient(ClientEventDTO clientEventDTO) {
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
}
