package br.com.clientservice.domain.event.listener;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.event.ClientCreatedEvent;
import br.com.clientservice.domain.event.ClientUpdatedEvent;
import br.com.clientservice.messaging.producer.ClientProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientEventListenerTest {

    @Mock
    private ClientProducer clientProducer;

    @InjectMocks
    private ClientEventListener underTest;

    @Test
    void handleClientCreated() throws Exception {
        var client = mock(ClientOutputDTO.class);
        when(client.id()).thenReturn(UUID.randomUUID());
        var event = new ClientCreatedEvent(client);

        underTest.handleClientCreated(event);

        verify(clientProducer).sendClientEvent("client-created-topic", client);
    }

    @Test
    void handleClientUpdated() throws Exception {
        var client = mock(ClientOutputDTO.class);
        when(client.id()).thenReturn(UUID.randomUUID());
        var event = new ClientUpdatedEvent(client);

        underTest.handleClientUpdated(event);

        verify(clientProducer).sendClientEvent("client-updated-topic", client);
    }

}
