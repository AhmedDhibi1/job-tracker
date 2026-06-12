package com.jobtracker.emailmanagement.application.command;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngestEmailCommandTest {

    private final UUID accountId = UUID.randomUUID();

    @Test
    void rejectsNullEmailAccountId() {
        assertThatThrownBy(() -> new IngestEmailCommand(null, "msg1", "corr-1"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("emailAccountId");
    }

    @Test
    void rejectsNullGmailMessageId() {
        assertThatThrownBy(() -> new IngestEmailCommand(accountId, null, "corr-1"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("gmailMessageId");
    }

    @Test
    void rejectsBlankGmailMessageId() {
        assertThatThrownBy(() -> new IngestEmailCommand(accountId, "   ", "corr-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gmailMessageId");
    }

    @Test
    void rejectsBlankCorrelationIdWhenProvided() {
        assertThatThrownBy(() -> new IngestEmailCommand(accountId, "msg1", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correlationId");
    }

    @Test
    void acceptsNullCorrelationId() {
        IngestEmailCommand cmd = new IngestEmailCommand(accountId, "msg1", null);
        assertThat(cmd.correlationId()).isNull();
    }

    @Test
    void preservesValues() {
        IngestEmailCommand cmd = new IngestEmailCommand(accountId, "msg123", "corr-xyz");
        assertThat(cmd.emailAccountId()).isEqualTo(accountId);
        assertThat(cmd.gmailMessageId()).isEqualTo("msg123");
        assertThat(cmd.correlationId()).isEqualTo("corr-xyz");
    }
}
