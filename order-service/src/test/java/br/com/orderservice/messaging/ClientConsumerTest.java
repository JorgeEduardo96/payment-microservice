package br.com.orderservice.messaging;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.repository.ClientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientConsumerTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientConsumer underTest;

    @Test
    void consume() throws JsonProcessingException {
        var message = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"name\":\"John Doe\"}";
        var clientEventDTO = new ClientEventDTO(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), "John Doe", LocalDateTime.now(), null);

        when(objectMapper.readValue(message, ClientEventDTO.class))
                .thenReturn(clientEventDTO);

        underTest.consume(message);

        verify(objectMapper).readValue(message, ClientEventDTO.class);
        verify(clientRepository).upsert(clientEventDTO);
    }

}
