package com.jobtracker.emailmanagement.domain.event;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailAccountRegisteredEventTest {

    @Test
    void constructor_setsAllFields() {
        UUID id = UUID.randomUUID();
        EmailAddress email = new EmailAddress("test@example.com");
        String correlationId = "corr-1";
        EmailAccountRegisteredEvent event = new EmailAccountRegisteredEvent(id, email, correlationId);
        assertThat(event.getAccountId()).isEqualTo(id);
        assertThat(event.getEmailAddress()).isEqualTo(email);
        assertThat(event.getCorrelationId()).isEqualTo(correlationId);
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}
