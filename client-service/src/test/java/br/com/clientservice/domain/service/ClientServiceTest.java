package br.com.clientservice.domain.service;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;
import br.com.clientservice.domain.event.ClientCreatedEvent;
import br.com.clientservice.domain.event.ClientUpdatedEvent;
import br.com.clientservice.domain.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ClientService underTest;

    @Test
    void insert() {
        ClientCreateInputDTO mockInputDTO = mock(ClientCreateInputDTO.class);
        ClientOutputDTO actualResult = mock(ClientOutputDTO.class);
        UUID generatedId = UUID.randomUUID();

        when(clientRepository.insert(mockInputDTO)).thenReturn(actualResult);
        when(actualResult.id()).thenReturn(generatedId);

        ClientOutputDTO expectedResult = underTest.insert(mockInputDTO);

        verify(applicationEventPublisher).publishEvent(any(ClientCreatedEvent.class));
        verify(clientRepository).insert(mockInputDTO);

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void update() {
        UUID clientId = UUID.randomUUID();
        ClientUpdateInputDTO mockInputDTO = mock(ClientUpdateInputDTO.class);
        ClientOutputDTO actualResult = mock(ClientOutputDTO.class);

        when(clientRepository.update(clientId, mockInputDTO)).thenReturn(actualResult);
        when(actualResult.id()).thenReturn(clientId);

        ClientOutputDTO expectedResult = underTest.update(clientId, mockInputDTO);

        verify(applicationEventPublisher).publishEvent(any(ClientUpdatedEvent.class));
        verify(clientRepository).update(clientId, mockInputDTO);

        assertThat(expectedResult).isEqualTo(actualResult);
    }

    @Test
    void findClient() {
        UUID clientId = UUID.randomUUID();
        ClientOutputDTO actualResult = mock(ClientOutputDTO.class);

        when(clientRepository.findById(clientId)).thenReturn(actualResult);

        ClientOutputDTO expectedResult = underTest.findClient(clientId);

        verify(clientRepository).findById(clientId);
        assertThat(expectedResult).isEqualTo(actualResult);
    }


}
