package br.com.clientservice.domain.repository;

import br.com.clientservice.domain.dto.OutboxInputDTO;
import br.com.clientservice.domain.entity.OutboxJpaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {

    UUID insert(OutboxInputDTO inputDTO);

    OutboxJpaEntity findById(UUID id);

    List<OutboxJpaEntity> findRetryableBatch(LocalDateTime olderThan);

    void markPublished(UUID id);

    void markFailed(UUID id, String lastError);

    void markProcessing(UUID id);
}
