package com.jobtracker.emailmanagement.domain.event;

import com.jobtracker.shared.domain.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public class OutboxEvent {

    private final UUID id;
    private final String aggregateType;
    private final UUID aggregateId;
    private final String eventType;
    private final String payload;
    private final Instant createdAt;
    private final boolean published;

    public OutboxEvent(UUID id, String aggregateType, UUID aggregateId,
                       String eventType, String payload, Instant createdAt, boolean published) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
        this.published = published;
    }

    public static OutboxEvent fromDomainEvent(DomainEvent event, String aggregateType, UUID aggregateId) {
        return new OutboxEvent(
                UUID.randomUUID(),
                aggregateType,
                aggregateId,
                event.getClass().getName(),
                event.toString(),
                Instant.now(),
                false
        );
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isPublished() { return published; }

    public OutboxEvent withPublished() {
        return new OutboxEvent(id, aggregateType, aggregateId, eventType, payload, createdAt, true);
    }
}
