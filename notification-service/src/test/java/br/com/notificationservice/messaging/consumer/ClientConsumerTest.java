package br.com.notificationservice.messaging.consumer;

import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.repository.ClientRepository;
import br.com.notificationservice.messaging.ClientConsumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientConsumerTest {


    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientConsumer underTest;

    @Test
    void consume() throws JsonProcessingException {
        var message = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"name\":\"John Doe\",\"createdAt\":\"2023-10-01T12:00:00Z\",\"updatedAt\":null}";
        var mockedClientEvent = mock(ClientEventDTO.class);

        when(objectMapper.readValue(message, ClientEventDTO.class))
                .thenReturn(mockedClientEvent);
        when(mockedClientEvent.id()).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

        underTest.consume(message);

        verify(objectMapper).readValue(message, ClientEventDTO.class);
        verify(clientRepository).upsert(mockedClientEvent);
    }

}
