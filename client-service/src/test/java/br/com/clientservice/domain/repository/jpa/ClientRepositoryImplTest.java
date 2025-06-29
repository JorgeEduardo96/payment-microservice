package br.com.clientservice.domain.repository.jpa;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;
import br.com.clientservice.domain.entity.ClientJpaEntity;
import br.com.clientservice.domain.mapper.ClientMapper;
import br.com.clientservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import br.com.sharedlib.model.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ClientRepositoryImplTest {

    @Mock
    private ClientMapper clientMapper;
    @Mock
    private ClientJpaEntityCrudRepository repository;

    @InjectMocks
    private ClientRepositoryImpl underTest;

    @Test
    void insert() {
        var inputDTO = mock(ClientCreateInputDTO.class);
        var entity = mock(ClientJpaEntity.class);
        var savedEntity = mock(ClientJpaEntity.class);
        var outputDTO = mock(ClientOutputDTO.class);

        when(clientMapper.toEntity(inputDTO)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(clientMapper.toDTO(savedEntity)).thenReturn(outputDTO);

        var result = underTest.insert(inputDTO);

        assertEquals(outputDTO, result);
        verify(entity).setCreatedAt(any(LocalDateTime.class));
        verify(repository).save(entity);
    }

    @Test
    void update() {
        var id = UUID.randomUUID();
        var inputDTO = mock(ClientUpdateInputDTO.class);
        var existingEntity = mock(ClientJpaEntity.class);
        var updatedEntity = mock(ClientJpaEntity.class);
        var outputDTO = mock(ClientOutputDTO.class);

        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(repository.save(existingEntity)).thenReturn(updatedEntity);
        when(clientMapper.toDTO(updatedEntity)).thenReturn(outputDTO);

        var result = underTest.update(id, inputDTO);

        assertEquals(outputDTO, result);
        verify(existingEntity).setUpdatedAt(any(LocalDateTime.class));
        verify(repository).save(existingEntity);
    }

    @Test
    void update_throwExceptionForNotFound() {
        var id = UUID.randomUUID();
        var inputDTO = mock(ClientUpdateInputDTO.class);

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> underTest.update(id, inputDTO));
    }

    @Test
    void findById() {
        var id = UUID.randomUUID();
        var entity = mock(ClientJpaEntity.class);
        var outputDTO = mock(ClientOutputDTO.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(clientMapper.toDTO(entity)).thenReturn(outputDTO);

        var result = underTest.findById(id);

        assertEquals(outputDTO, result);
    }

    @Test
    void findById_throwExceptionForNotFound() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> underTest.findById(id));
    }

    @Test
    void findByEmail() {
        var email = "test@email.com";
        var entity = mock(ClientJpaEntity.class);
        var outputDTO = mock(ClientOutputDTO.class);

        when(repository.findByEmail(email)).thenReturn(Optional.of(entity));
        when(clientMapper.toDTO(entity)).thenReturn(outputDTO);

        var result = underTest.findByEmail(email);

        assertEquals(outputDTO, result);
    }

    @Test
    void findByEmail_returningNull() {
        var email = "notfound@email.com";
        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        assertNull(underTest.findByEmail(email));
    }

    @Test
    void findByCpf() {
        var cpf = "12345678900";
        var entity = mock(ClientJpaEntity.class);
        var outputDTO = mock(ClientOutputDTO.class);

        when(repository.findByCpf(cpf)).thenReturn(Optional.of(entity));
        when(clientMapper.toDTO(entity)).thenReturn(outputDTO);

        var result = underTest.findByCpf(cpf);

        assertEquals(outputDTO, result);
    }

    @Test
    void findByCpf_returningNull() {
        var cpf = "00000000000";
        when(repository.findByCpf(cpf)).thenReturn(Optional.empty());

        assertNull(underTest.findByCpf(cpf));
    }
}
