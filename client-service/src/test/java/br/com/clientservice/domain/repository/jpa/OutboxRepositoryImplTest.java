package br.com.clientservice.domain.repository.jpa;

import br.com.clientservice.domain.dto.OutboxInputDTO;
import br.com.clientservice.domain.entity.OutboxJpaEntity;
import br.com.clientservice.domain.repository.jpa.crudrepository.OutboxJpaEntityCrudRepository;
import br.com.sharedlib.model.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRepositoryImplTest {

    @Mock
    private OutboxJpaEntityCrudRepository outboxJpaEntityCrudRepository;

    @InjectMocks
    private OutboxRepositoryImpl underTest;

    @Test
    void insertPersistsEntityBuiltFromInputDTOAndReturnsGeneratedId() {
        var inputDTO = new OutboxInputDTO("Client", "aggregate-id", "client-created", "{\"id\":\"1\"}");
        var generatedId = UUID.randomUUID();

        var savedCaptor = ArgumentCaptor.forClass(OutboxJpaEntity.class);
        var savedEntity = new OutboxJpaEntity();
        savedEntity.setId(generatedId);
        when(outboxJpaEntityCrudRepository.save(savedCaptor.capture())).thenReturn(savedEntity);

        var result = underTest.insert(inputDTO);

        assertThat(result).isEqualTo(generatedId);
        var captured = savedCaptor.getValue();
        assertThat(captured.getAggregateType()).isEqualTo("Client");
        assertThat(captured.getAggregateId()).isEqualTo("aggregate-id");
        assertThat(captured.getEventType()).isEqualTo("client-created");
        assertThat(captured.getPayload()).isEqualTo("{\"id\":\"1\"}");
    }

    @Test
    void findByIdReturnsEntityWhenPresent() {
        var id = UUID.randomUUID();
        var entity = new OutboxJpaEntity();
        entity.setId(id);

        when(outboxJpaEntityCrudRepository.findById(id)).thenReturn(Optional.of(entity));

        var result = underTest.findById(id);

        assertThat(result).isEqualTo(entity);
    }

    @Test
    void findByIdThrowsEntityNotFoundExceptionWhenAbsent() {
        var id = UUID.randomUUID();
        when(outboxJpaEntityCrudRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findRetryableBatchDelegatesToCrudRepositoryWithPendingAndFailedStatuses() {
        var olderThan = LocalDateTime.now().minusSeconds(30);
        var entity = new OutboxJpaEntity();
        when(outboxJpaEntityCrudRepository.findTop20ByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
                eq(List.of("PENDING", "FAILED")), eq(olderThan)))
                .thenReturn(List.of(entity));

        var result = underTest.findRetryableBatch(olderThan);

        assertThat(result).containsExactly(entity);
    }

    @Test
    void markPublishedDelegatesToCrudRepository() {
        var id = UUID.randomUUID();

        underTest.markPublished(id);

        verify(outboxJpaEntityCrudRepository).markPublished(id);
    }

    @Test
    void markFailedDelegatesToCrudRepository() {
        var id = UUID.randomUUID();

        underTest.markFailed(id, "boom");

        verify(outboxJpaEntityCrudRepository).markFailed(id, "boom");
    }

    @Test
    void markProcessingDelegatesToCrudRepository() {
        var id = UUID.randomUUID();

        underTest.markProcessing(id);

        verify(outboxJpaEntityCrudRepository).markProcessing(id);
    }
}
