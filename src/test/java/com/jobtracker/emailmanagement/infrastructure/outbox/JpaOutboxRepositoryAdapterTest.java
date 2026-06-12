package com.jobtracker.emailmanagement.infrastructure.outbox;

import com.jobtracker.emailmanagement.domain.event.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaOutboxRepositoryAdapterTest {

    @Mock SpringDataOutboxRepository springDataRepository;
    @InjectMocks JpaOutboxRepositoryAdapter adapter;

    @Test
    void save_persistsEntity() {
        OutboxEvent event = new OutboxEvent(UUID.randomUUID(), "Order", UUID.randomUUID(),
                "OrderCreated", "{}", Instant.now(), false);

        adapter.save(event);

        verify(springDataRepository).save(any(OutboxEventJpaEntity.class));
    }

    @Test
    void findUnpublished_returnsMappedEvents() {
        UUID id = UUID.randomUUID();
        UUID aggId = UUID.randomUUID();
        Instant now = Instant.now();
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(id);
        entity.setAggregateType("Order");
        entity.setAggregateId(aggId);
        entity.setEventType("OrderCreated");
        entity.setPayload("{}");
        entity.setCreatedAt(now);
        entity.setPublished(false);

        when(springDataRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(entity));

        List<OutboxEvent> result = adapter.findUnpublished(10);

        assertThat(result).hasSize(1);
        OutboxEvent event = result.get(0);
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getAggregateType()).isEqualTo("Order");
        assertThat(event.getAggregateId()).isEqualTo(aggId);
        assertThat(event.getEventType()).isEqualTo("OrderCreated");
        assertThat(event.getPayload()).isEqualTo("{}");
        assertThat(event.getCreatedAt()).isEqualTo(now);
        assertThat(event.isPublished()).isFalse();
    }

    @Test
    void findUnpublished_limitsResults() {
        when(springDataRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(new OutboxEventJpaEntity(), new OutboxEventJpaEntity(), new OutboxEventJpaEntity()));

        List<OutboxEvent> result = adapter.findUnpublished(2);

        assertThat(result).hasSize(2);
    }

    @Test
    void markPublished_updatesEntity() {
        UUID id = UUID.randomUUID();
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(id);
        entity.setPublished(false);
        when(springDataRepository.findById(id)).thenReturn(Optional.of(entity));

        adapter.markPublished(id);

        assertThat(entity.isPublished()).isTrue();
        verify(springDataRepository).save(entity);
    }

    @Test
    void markPublished_doesNothingWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(springDataRepository.findById(id)).thenReturn(Optional.empty());

        adapter.markPublished(id);

        verify(springDataRepository, never()).save(any());
    }
}
