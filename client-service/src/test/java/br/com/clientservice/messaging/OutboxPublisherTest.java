package br.com.clientservice.messaging;

import br.com.clientservice.domain.entity.OutboxJpaEntity;
import br.com.clientservice.domain.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private ClientProducer clientProducer;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxPublisher underTest;

    @Test
    void publish() throws Exception {
        var outboxId = UUID.randomUUID();
        var outbox = new OutboxJpaEntity();
        outbox.setId(outboxId);
        outbox.setEventType("client-created");
        outbox.setPayload("{\"id\":\"1\"}");

        var client = mock(br.com.clientservice.domain.dto.ClientOutputDTO.class);

        when(outboxRepository.findById(outboxId)).thenReturn(outbox);
        when(objectMapper.readValue("{\"id\":\"1\"}", br.com.clientservice.domain.dto.ClientOutputDTO.class)).thenReturn(client);

        underTest.publish(outboxId);

        verify(clientProducer).sendClientEvent("client-created-topic", client);
        verify(outboxRepository).markPublished(outboxId);
    }

    @Test
    void publishRetryableBatch() throws Exception {
        var outboxId = UUID.randomUUID();
        var outbox = new OutboxJpaEntity();
        outbox.setId(outboxId);
        outbox.setEventType("client-updated");
        outbox.setPayload("{\"id\":\"2\"}");

        var client = mock(br.com.clientservice.domain.dto.ClientOutputDTO.class);

        when(outboxRepository.findRetryableBatch(any())).thenReturn(List.of(outbox));
        when(outboxRepository.findById(outboxId)).thenReturn(outbox);
        when(objectMapper.readValue("{\"id\":\"2\"}", br.com.clientservice.domain.dto.ClientOutputDTO.class)).thenReturn(client);

        underTest.publishRetryableBatch();

        verify(outboxRepository).markProcessing(outboxId);
        verify(clientProducer).sendClientEvent("client-updated-topic", client);
        verify(outboxRepository).markPublished(outboxId);
    }

    @Test
    void publishMarksOutboxAsFailedWhenKafkaSendThrows() throws Exception {
        var outboxId = UUID.randomUUID();
        var outbox = new OutboxJpaEntity();
        outbox.setId(outboxId);
        outbox.setEventType("client-created");
        outbox.setPayload("{\"id\":\"1\"}");

        var client = mock(br.com.clientservice.domain.dto.ClientOutputDTO.class);

        when(outboxRepository.findById(outboxId)).thenReturn(outbox);
        when(objectMapper.readValue("{\"id\":\"1\"}", br.com.clientservice.domain.dto.ClientOutputDTO.class)).thenReturn(client);
        doThrow(new RuntimeException("Kafka unavailable"))
                .when(clientProducer).sendClientEvent("client-created-topic", client);

        underTest.publish(outboxId);

        verify(outboxRepository).markFailed(outboxId, "Kafka unavailable");
        verify(outboxRepository, never()).markPublished(any());
    }

    @Test
    void publishMarksOutboxAsFailedWhenEventTypeIsUnsupported() {
        var outboxId = UUID.randomUUID();
        var outbox = new OutboxJpaEntity();
        outbox.setId(outboxId);
        outbox.setEventType("unknown-event");
        outbox.setPayload("{\"id\":\"1\"}");

        when(outboxRepository.findById(outboxId)).thenReturn(outbox);

        underTest.publish(outboxId);

        verify(outboxRepository).markFailed(eq(outboxId), anyString());
        verify(clientProducer, never()).sendClientEvent(anyString(), any());
        verify(outboxRepository, never()).markPublished(any());
    }

    @Test
    void publishRetryableBatchMarksOutboxAsFailedWhenKafkaSendThrows() throws Exception {
        var outboxId = UUID.randomUUID();
        var outbox = new OutboxJpaEntity();
        outbox.setId(outboxId);
        outbox.setEventType("client-updated");
        outbox.setPayload("{\"id\":\"2\"}");

        var client = mock(br.com.clientservice.domain.dto.ClientOutputDTO.class);

        when(outboxRepository.findRetryableBatch(any())).thenReturn(List.of(outbox));
        when(outboxRepository.findById(outboxId)).thenReturn(outbox);
        when(objectMapper.readValue("{\"id\":\"2\"}", br.com.clientservice.domain.dto.ClientOutputDTO.class)).thenReturn(client);
        doThrow(new RuntimeException("Kafka unavailable"))
                .when(clientProducer).sendClientEvent("client-updated-topic", client);

        underTest.publishRetryableBatch();

        verify(outboxRepository).markProcessing(outboxId);
        verify(outboxRepository).markFailed(outboxId, "Kafka unavailable");
        verify(outboxRepository, never()).markPublished(any());
    }
}
