package com.jobtracker.shared.domain.event;

import java.time.Instant;
import java.util.UUID;


public abstract class DomainEvent {

   
    private final UUID eventId;

   
    private final Instant occurredAt;

    
    private final String correlationId;

    
    protected DomainEvent(String correlationId) {
        this.eventId       = UUID.randomUUID();
        this.occurredAt    = Instant.now();
        this.correlationId = correlationId;
    }

   
    public UUID getEventId() {
        return eventId;
    }

   
    public Instant getOccurredAt() {
        return occurredAt;
    }

    
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{eventId=" + eventId
                + ", occurredAt=" + occurredAt
                + ", correlationId='" + correlationId + "'}";
    }
}