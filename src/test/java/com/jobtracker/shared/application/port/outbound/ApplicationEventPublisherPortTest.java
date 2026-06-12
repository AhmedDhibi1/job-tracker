package com.jobtracker.shared.application.port.outbound;

import com.jobtracker.emailmanagement.domain.event.EmailAccountRegisteredEvent;
import com.jobtracker.shared.domain.event.DomainEvent;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationEventPublisherPortTest {

    @Test
    void publish_invocation() {
        AtomicReference<DomainEvent> captured = new AtomicReference<>();
        ApplicationEventPublisherPort port = captured::set;

        DomainEvent event = new EmailAccountRegisteredEvent(
                UUID.randomUUID(), new EmailAddress("test@example.com"), "corr-1");
        port.publish(event);

        assertThat(captured.get()).isSameAs(event);
    }
}
