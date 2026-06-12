package com.jobtracker.shared.domain.event;

import com.jobtracker.emailmanagement.domain.event.EmailAccountRegisteredEvent;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventTest {

    @Test
    void constructor_setsEventIdAndOccurredAt() {
        DomainEvent event = new EmailAccountRegisteredEvent(
                UUID.randomUUID(), new EmailAddress("test@example.com"), "corr-1");
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getCorrelationId()).isEqualTo("corr-1");
    }

    @Test
    void eachEventHasUniqueId() {
        DomainEvent e1 = new EmailAccountRegisteredEvent(
                UUID.randomUUID(), new EmailAddress("a@b.com"), "c1");
        DomainEvent e2 = new EmailAccountRegisteredEvent(
                UUID.randomUUID(), new EmailAddress("a@b.com"), "c2");
        assertThat(e1.getEventId()).isNotEqualTo(e2.getEventId());
    }

    @Test
    void toString_containsClassNameAndFields() {
        DomainEvent event = new EmailAccountRegisteredEvent(
                UUID.randomUUID(), new EmailAddress("test@example.com"), "corr-1");
        String str = event.toString();
        assertThat(str).contains("EmailAccountRegisteredEvent");
        assertThat(str).contains("corr-1");
        assertThat(str).contains(event.getEventId().toString());
    }
}
