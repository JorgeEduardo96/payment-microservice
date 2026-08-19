package br.com.paymentservice.domain.repository.jpa;

import br.com.paymentservice.domain.dto.OutboxInputDTO;
import br.com.paymentservice.domain.entity.OutboxJpaEntity;
import br.com.paymentservice.domain.repository.OutboxRepository;
import br.com.paymentservice.domain.repository.jpa.crudrepository.OutboxJpaEntityCrudRepository;
import br.com.sharedlib.model.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final OutboxJpaEntityCrudRepository outboxJpaEntityCrudRepository;

    @Override
    @Transactional
    public UUID insert(OutboxInputDTO inputDTO) {
        var entity = new OutboxJpaEntity();
        entity.setAggregateType(inputDTO.aggregateType());
        entity.setAggregateId(inputDTO.aggregateId());
        entity.setEventType(inputDTO.eventType());
        entity.setPayload(inputDTO.payload());
        return outboxJpaEntityCrudRepository.save(entity).getId();
    }

    @Override
    public OutboxJpaEntity findById(UUID id) {
        return outboxJpaEntityCrudRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Outbox", id.toString()));
    }

    @Override
    public List<OutboxJpaEntity> findRetryableBatch(LocalDateTime olderThan) {
        return outboxJpaEntityCrudRepository.findTop20ByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
                List.of("PENDING", "FAILED"),
                olderThan
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(UUID id) {
        outboxJpaEntityCrudRepository.markPublished(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID id, String lastError) {
        outboxJpaEntityCrudRepository.markFailed(id, lastError);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(UUID id) {
        outboxJpaEntityCrudRepository.markProcessing(id);
    }
}
