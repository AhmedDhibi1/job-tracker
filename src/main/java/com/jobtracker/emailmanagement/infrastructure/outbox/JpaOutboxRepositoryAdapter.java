package com.jobtracker.emailmanagement.infrastructure.outbox;

import com.jobtracker.emailmanagement.application.port.outbound.OutboxRepository;
import com.jobtracker.emailmanagement.domain.event.OutboxEvent;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaOutboxRepositoryAdapter implements OutboxRepository {

    private final SpringDataOutboxRepository springDataRepository;

    public JpaOutboxRepositoryAdapter(SpringDataOutboxRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    @Transactional
    public void save(OutboxEvent event) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(event.getId());
        entity.setAggregateType(event.getAggregateType());
        entity.setAggregateId(event.getAggregateId());
        entity.setEventType(event.getEventType());
        entity.setPayload(event.getPayload());
        entity.setCreatedAt(event.getCreatedAt());
        entity.setPublished(event.isPublished());
        springDataRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findUnpublished(int limit) {
        return springDataRepository.findByPublishedFalseOrderByCreatedAtAsc()
                .stream()
                .limit(limit)
                .map(e -> new OutboxEvent(
                        e.getId(), e.getAggregateType(), e.getAggregateId(),
                        e.getEventType(), e.getPayload(), e.getCreatedAt(), e.isPublished()))
                .toList();
    }

    @Override
    @Transactional
    public void markPublished(UUID eventId) {
        springDataRepository.findById(eventId).ifPresent(entity -> {
            entity.setPublished(true);
            springDataRepository.save(entity);
        });
    }
}
