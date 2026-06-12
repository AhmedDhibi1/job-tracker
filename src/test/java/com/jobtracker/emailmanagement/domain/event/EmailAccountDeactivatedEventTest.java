package com.jobtracker.emailmanagement.domain.event;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailAccountDeactivatedEventTest {

    @Test
    void constructor_setsAllFields() {
        UUID id = UUID.randomUUID();
        EmailAddress email = new EmailAddress("test@example.com");
        String reason = "User requested deactivation";
        String correlationId = "corr-1";
        EmailAccountDeactivatedEvent event = new EmailAccountDeactivatedEvent(id, email, reason, correlationId);
        assertThat(event.getAccountId()).isEqualTo(id);
        assertThat(event.getEmailAddress()).isEqualTo(email);
        assertThat(event.getReason()).isEqualTo(reason);
        assertThat(event.getCorrelationId()).isEqualTo(correlationId);
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}
