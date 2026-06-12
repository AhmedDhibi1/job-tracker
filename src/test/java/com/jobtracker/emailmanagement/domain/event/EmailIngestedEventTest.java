package com.jobtracker.emailmanagement.domain.event;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailIngestedEventTest {

    @Test
    void constructor_setsAllFields() {
        UUID msgId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        String gmailMessageId = "gmail123";
        String correlationId = "corr-1";
        EmailIngestedEvent event = new EmailIngestedEvent(msgId, gmailMessageId, accountId, correlationId);
        assertThat(event.getEmailMessageId()).isEqualTo(msgId);
        assertThat(event.getGmailMessageId()).isEqualTo(gmailMessageId);
        assertThat(event.getAccountId()).isEqualTo(accountId);
        assertThat(event.getCorrelationId()).isEqualTo(correlationId);
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}
