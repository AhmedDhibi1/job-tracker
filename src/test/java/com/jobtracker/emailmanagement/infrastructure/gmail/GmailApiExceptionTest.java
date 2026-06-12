package com.jobtracker.emailmanagement.infrastructure.gmail;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GmailApiExceptionTest {

    @Test
    void constructor_setsAllFields() {
        Exception cause = new RuntimeException("API error");
        GmailApiException ex = new GmailApiException("Fetch failed", cause,
                "account-123", GmailApiException.Operation.FETCH_MESSAGE);
        assertThat(ex.getMessage()).isEqualTo("Fetch failed");
        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex.getAccountId()).isEqualTo("account-123");
        assertThat(ex.getOperation()).isEqualTo(GmailApiException.Operation.FETCH_MESSAGE);
    }

    @Test
    void operation_enum_hasAllValues() {
        assertThat(GmailApiException.Operation.values()).containsExactlyInAnyOrder(
                GmailApiException.Operation.FETCH_MESSAGE,
                GmailApiException.Operation.LIST_MESSAGES,
                GmailApiException.Operation.FETCH_HISTORY,
                GmailApiException.Operation.SETUP_WATCH,
                GmailApiException.Operation.STOP_WATCH
        );
    }
}
