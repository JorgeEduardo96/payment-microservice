package br.com.orderservice.domain.repository.jpa;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.entity.ClientJpaEntity;
import br.com.orderservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import br.com.orderservice.mapper.ClientMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientRepositoryImplTest {

    @Mock
    private ClientMapper clientMapper;
    @Mock
    private ClientJpaEntityCrudRepository repository;

    @InjectMocks
    private ClientRepositoryImpl underTest;

    @Test
    void findById() {
        var id = UUID.randomUUID();
        ClientJpaEntity mockedEntity = mock(ClientJpaEntity.class);
        var expectedResult = mock(ClientEventDTO.class);

        when(repository.findById(id)).thenReturn(Optional.of(mockedEntity));
        when(clientMapper.toDTO(mockedEntity)).thenReturn(expectedResult);

        var result = underTest.findById(id);

        assertThat(result).isEqualTo(Optional.of(expectedResult));
        verify(repository).findById(id);
        verify(clientMapper).toDTO(mockedEntity);
    }

    @Test
    void upsert() {
        var dto = mock(ClientEventDTO.class);
        ClientJpaEntity mockedEntity = mock(ClientJpaEntity.class);

        when(clientMapper.toEntity(dto)).thenReturn(mockedEntity);

        underTest.upsert(dto);

        verify(clientMapper).toEntity(dto);
        verify(repository).save(mockedEntity);
    }

}
