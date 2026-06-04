package com.jobtracker.shared.domain.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DomainEvent}.
 *
 * Uses a package-private concrete stub to exercise the abstract class
 * without introducing a production subclass.
 */
class DomainEventTest {

    // ── Concrete stub ────────────────────────────────────────────────────────

    static final class TestEvent extends DomainEvent {
        TestEvent(String correlationId) {
            super(correlationId);
        }
    }

    // ── eventId ──────────────────────────────────────────────────────────────

    @Test
    void eventId_isNotNull_onCreation() {
        var event = new TestEvent("corr-001");
        assertThat(event.getEventId()).isNotNull();
    }

    @Test
    void twoEvents_haveDistinct_eventIds() {
        var event1 = new TestEvent("corr-001");
        var event2 = new TestEvent("corr-001");
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    // ── occurredAt ───────────────────────────────────────────────────────────

    @Test
    void occurredAt_isNotNull_onCreation() {
        var event = new TestEvent("corr-001");
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void occurredAt_isCloseToNow_onCreation() {
        Instant before = Instant.now().minusMillis(100);
        var event      = new TestEvent("corr-001");
        Instant after  = Instant.now().plusMillis(100);

        assertThat(event.getOccurredAt())
                .isAfter(before)
                .isBefore(after);
    }

    // ── correlationId ────────────────────────────────────────────────────────

    @Test
    void correlationId_isPreserved_fromConstructor() {
        var event = new TestEvent("my-trace-id-abc");
        assertThat(event.getCorrelationId()).isEqualTo("my-trace-id-abc");
    }

    @Test
    void correlationId_canBeNull_whenNoMdcContextIsActive() {
        var event = new TestEvent(null);
        assertThat(event.getCorrelationId()).isNull();
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    void toString_containsSimpleClassName() {
        var event = new TestEvent("corr-001");
        assertThat(event.toString()).contains("TestEvent");
    }

    @Test
    void toString_containsEventId() {
        var event = new TestEvent("corr-001");
        assertThat(event.toString()).contains(event.getEventId().toString());
    }

    // ── Framework isolation ──────────────────────────────────────────────────

    @Test
    void domainEvent_hasNoSpringDependencies() {
        // If this test compiles and runs, the class has no Spring context requirement.
        // This test exists as a documentation checkpoint.
        var event = new TestEvent("corr-isolation-check");
        assertThat(event).isNotNull();
    }
}