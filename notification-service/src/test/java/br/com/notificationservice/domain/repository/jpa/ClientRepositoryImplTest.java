package br.com.notificationservice.domain.repository.jpa;

import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.entity.ClientJpaEntity;
import br.com.notificationservice.domain.exception.EntityNotFoundException;
import br.com.notificationservice.domain.mapper.ClientMapper;
import br.com.notificationservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientRepositoryImplTest {

    @Mock
    private ClientMapper mapper;
    @Mock
    private ClientJpaEntityCrudRepository repository;

    @InjectMocks
    private ClientRepositoryImpl underTest;

    @Test
    void upsertClient_existentClient() {
        UUID id = UUID.randomUUID();
        ClientEventDTO mockedDto = mock(ClientEventDTO.class);
        ClientJpaEntity mockedExistingEntity = mock(ClientJpaEntity.class);

        when(mockedDto.id()).thenReturn(id);
        when(mockedDto.name()).thenReturn("John Doe");
        when(mockedDto.email()).thenReturn("john.doe@email.com");
        when(repository.findById(mockedDto.id())).thenReturn(Optional.of(mockedExistingEntity));

        underTest.upsertClient(mockedDto);

        verify(mockedExistingEntity).setName("John Doe");
        verify(mockedExistingEntity).setEmail("john.doe@email.com");
        verify(repository).save(mockedExistingEntity);
    }

    @Test
    void upsertClient_nonExistentClient() {
        UUID id = UUID.randomUUID();
        ClientEventDTO mockedDto = mock(ClientEventDTO.class);
        ClientJpaEntity newEntity = mock(ClientJpaEntity.class);

        when(mockedDto.id()).thenReturn(id);
        when(repository.findById(mockedDto.id())).thenReturn(Optional.empty());
        when(mapper.toEntity(mockedDto)).thenReturn(newEntity);

        underTest.upsertClient(mockedDto);

        verify(repository).save(newEntity);
    }

    @Test
    void findById_existingClient() {
        UUID id = UUID.randomUUID();
        ClientJpaEntity entity = mock(ClientJpaEntity.class);
        ClientEventDTO mockedDto = mock(ClientEventDTO.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(mockedDto);

        ClientEventDTO result = underTest.findById(id);

        assertThat(mockedDto).isEqualTo(result);
        verify(mapper).toDto(entity);
    }

    @Test
    void findById_nonExistingClient() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> underTest.findById(id));

        assertTrue(exception.getMessage().contains("Client"));
        assertTrue(exception.getMessage().contains(id.toString()));
    }
}
