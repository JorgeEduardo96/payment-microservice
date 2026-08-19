package br.com.clientservice.domain.event.listener;

import br.com.clientservice.domain.event.OutboxCreatedEvent;
import br.com.clientservice.messaging.OutboxPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ClientEventListenerTest {

    @Mock
    private OutboxPublisher outboxPublisher;

    @InjectMocks
    private EventListener underTest;

    @Test
    void handleOutboxCreated() {
        var outboxId = UUID.randomUUID();

        underTest.handleOutboxCreated(new OutboxCreatedEvent(outboxId));

        verify(outboxPublisher).publish(outboxId);
    }

}
