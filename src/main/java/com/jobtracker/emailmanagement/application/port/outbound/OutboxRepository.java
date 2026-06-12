package com.jobtracker.emailmanagement.application.port.outbound;

import com.jobtracker.emailmanagement.domain.event.OutboxEvent;
import java.util.List;

public interface OutboxRepository {
    void save(OutboxEvent event);
    List<OutboxEvent> findUnpublished(int limit);
    void markPublished(java.util.UUID eventId);
}
