package br.com.paymentservice.domain.repository.jpa.crudrepository;

import br.com.paymentservice.domain.entity.OutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxJpaEntityCrudRepository extends JpaRepository<OutboxJpaEntity, UUID> {

    List<OutboxJpaEntity> findTop20ByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(Collection<String> statuses, LocalDateTime createdAt);

    List<OutboxJpaEntity> findByAggregateId(String aggregateId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update OutboxJpaEntity o
            set o.status = 'PUBLISHED',
                o.publishedAt = CURRENT_TIMESTAMP
            where o.id = :id
            """)
    void markPublished(@Param("id") UUID id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update OutboxJpaEntity o
            set o.status = 'FAILED',
                o.attempts = coalesce(o.attempts, 0) + 1,
                o.lastError = :lastError
            where o.id = :id
            """)
    void markFailed(@Param("id") UUID id, @Param("lastError") String lastError);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update OutboxJpaEntity o
            set o.status = 'PROCESSING'
            where o.id = :id
            """)
    void markProcessing(@Param("id") UUID id);
}
