package com.jobtracker.emailmanagement.domain.event;

import com.jobtracker.shared.domain.event.DomainEvent;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    @Test
    void fromDomainEvent_createsOutboxEvent() {
        DomainEvent event = new EmailAccountRegisteredEvent(
                UUID.randomUUID(), new EmailAddress("test@example.com"), "corr-1");
        OutboxEvent outbox = OutboxEvent.fromDomainEvent(event, "EmailAccount", UUID.randomUUID());
        assertThat(outbox.getId()).isNotNull();
        assertThat(outbox.getAggregateType()).isEqualTo("EmailAccount");
        assertThat(outbox.getEventType()).isEqualTo(event.getClass().getName());
        assertThat(outbox.getPayload()).isNotNull();
        assertThat(outbox.getCreatedAt()).isNotNull();
        assertThat(outbox.isPublished()).isFalse();
    }

    @Test
    void fromDomainEvent_containsEventClassNameAsType() {
        DomainEvent event = new EmailAccountRegisteredEvent(
                UUID.randomUUID(), new EmailAddress("test@example.com"), "corr-2");
        OutboxEvent outbox = OutboxEvent.fromDomainEvent(event, "EmailAccount", UUID.randomUUID());
        assertThat(outbox.getEventType()).contains("EmailAccountRegisteredEvent");
    }

    @Test
    void withPublished_returnsNewInstanceWithPublishedTrue() {
        DomainEvent event = new EmailAccountRegisteredEvent(
                UUID.randomUUID(), new EmailAddress("test@example.com"), "corr-3");
        OutboxEvent original = OutboxEvent.fromDomainEvent(event, "EmailAccount", UUID.randomUUID());
        OutboxEvent published = original.withPublished();
        assertThat(original.isPublished()).isFalse();
        assertThat(published.isPublished()).isTrue();
        assertThat(published.getId()).isEqualTo(original.getId());
        assertThat(published.getAggregateType()).isEqualTo(original.getAggregateType());
        assertThat(published.getEventType()).isEqualTo(original.getEventType());
    }

    @Test
    void constructor_setsAllFields() {
        UUID id = UUID.randomUUID();
        UUID aggId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(id, "Order", aggId, "OrderCreated", "{}", java.time.Instant.now(), true);
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getAggregateType()).isEqualTo("Order");
        assertThat(event.getAggregateId()).isEqualTo(aggId);
        assertThat(event.getEventType()).isEqualTo("OrderCreated");
        assertThat(event.getPayload()).isEqualTo("{}");
        assertThat(event.isPublished()).isTrue();
    }
}
