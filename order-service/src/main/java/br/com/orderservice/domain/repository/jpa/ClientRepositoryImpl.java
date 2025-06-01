package br.com.orderservice.domain.repository.jpa;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.repository.ClientRepository;
import br.com.orderservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import br.com.orderservice.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientRepositoryImpl implements ClientRepository {

    private final ClientMapper mapper;
    private final ClientJpaEntityCrudRepository repository;

    @Override
    public Optional<ClientEventDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toDTO);
    }

    @Override
    public void upsert(ClientEventDTO dto) {
        var entity = mapper.toEntity(dto);
        repository.save(entity);
    }

}
