package com.jobtracker.shared.application.port.outbound;

import com.jobtracker.shared.domain.event.DomainEvent;

/**
 * Outbound port for publishing domain events. All bounded contexts use this
 * port to remain decoupled from Spring's event infrastructure.
 */
public interface ApplicationEventPublisherPort {
    void publish(DomainEvent event);
}