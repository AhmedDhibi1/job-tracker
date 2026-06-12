package com.jobtracker.emailmanagement.infrastructure.outbox;

import com.jobtracker.emailmanagement.application.port.outbound.OutboxRepository;
import com.jobtracker.emailmanagement.domain.event.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    @Mock OutboxRepository outboxRepository;
    @InjectMocks OutboxPoller poller;

    @Test
    void publishOutboxEvents_marksAllAsPublished() {
        OutboxEvent event1 = new OutboxEvent(UUID.randomUUID(), "Test", UUID.randomUUID(),
                "TestEvent", "{}", java.time.Instant.now(), false);
        OutboxEvent event2 = new OutboxEvent(UUID.randomUUID(), "Test", UUID.randomUUID(),
                "TestEvent", "{}", java.time.Instant.now(), false);
        when(outboxRepository.findUnpublished(100)).thenReturn(List.of(event1, event2));

        poller.publishOutboxEvents();

        verify(outboxRepository).markPublished(event1.getId());
        verify(outboxRepository).markPublished(event2.getId());
    }

    @Test
    void publishOutboxEvents_doesNothingWhenNoEvents() {
        when(outboxRepository.findUnpublished(100)).thenReturn(List.of());

        poller.publishOutboxEvents();

        verify(outboxRepository, never()).markPublished(any());
    }

    @Test
    void publishOutboxEvents_continuesOnError() {
        OutboxEvent event1 = new OutboxEvent(UUID.randomUUID(), "Test", UUID.randomUUID(),
                "TestEvent", "{}", java.time.Instant.now(), false);
        OutboxEvent event2 = new OutboxEvent(UUID.randomUUID(), "Test", UUID.randomUUID(),
                "TestEvent", "{}", java.time.Instant.now(), false);
        when(outboxRepository.findUnpublished(100)).thenReturn(List.of(event1, event2));
        doThrow(new RuntimeException("DB error")).when(outboxRepository).markPublished(event1.getId());

        poller.publishOutboxEvents();

        verify(outboxRepository).markPublished(event1.getId());
        verify(outboxRepository).markPublished(event2.getId());
    }
}
