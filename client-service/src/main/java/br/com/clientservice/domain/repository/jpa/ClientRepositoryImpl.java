package br.com.clientservice.domain.repository.jpa;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;
import br.com.clientservice.domain.exception.EntityNotFoundException;
import br.com.clientservice.domain.mapper.ClientMapper;
import br.com.clientservice.domain.repository.ClientRepository;
import br.com.clientservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientRepositoryImpl implements ClientRepository {

    private final ClientMapper mapper;
    private final ClientJpaEntityCrudRepository repository;

    @Override
    public ClientOutputDTO insert(ClientCreateInputDTO inputDTO) {
        var entity = mapper.toEntity(inputDTO);
        entity.setCreatedAt(LocalDateTime.now());
        var savedEntity = repository.save(entity);
        return mapper.toDTO(savedEntity);
    }

    @Override
    public ClientOutputDTO update(UUID id, ClientUpdateInputDTO inputDTO) {
        var existingEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client", id));
         BeanUtils.copyProperties(inputDTO, existingEntity, getNullPropertyNames(inputDTO));
        existingEntity.setUpdatedAt(LocalDateTime.now());
        var updatedEntity = repository.save(existingEntity);
        return mapper.toDTO(updatedEntity);
    }

    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        Set<String> emptyNames = new HashSet<>();
        for (var propertyDescriptor : src.getPropertyDescriptors()) {
            Object value = src.getPropertyValue(propertyDescriptor.getName());
            if (value == null) {
                emptyNames.add(propertyDescriptor.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }

    @Override
    public ClientOutputDTO findById(UUID id) {
        return mapper.toDTO(repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Client", id)));
    }

    @Override
    public ClientOutputDTO findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDTO).orElse(null);
    }

    @Override
    public ClientOutputDTO findByCpf(String cpf) {
        return repository.findByCpf(cpf).map(mapper::toDTO).orElse(null);
    }
}
