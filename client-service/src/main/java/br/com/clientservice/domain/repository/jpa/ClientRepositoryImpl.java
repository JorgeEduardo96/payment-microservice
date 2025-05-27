package br.com.clientservice.domain.repository.jpa;

import br.com.clientservice.domain.dto.ClientInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.entity.ClientJpaEntity;
import br.com.clientservice.domain.exception.EntityNotFoundException;
import br.com.clientservice.domain.mapper.ClientMapper;
import br.com.clientservice.domain.repository.ClientRepository;
import br.com.clientservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientRepositoryImpl implements ClientRepository {

    private final ClientMapper mapper;
    private final ClientJpaEntityCrudRepository repository;

    @Override
    public ClientOutputDTO insert(ClientInputDTO inputDTO) {
        return mapper.toDTO(repository.save(mapper.toEntity(null, inputDTO)));
    }

    @Override
    public ClientOutputDTO findById(UUID id) {
        return mapper.toDTO(repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Client", id)));
    }
}
