package com.jobtracker.shared.application.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrentModificationExceptionTest {

    @Test
    void constructor_setsCorrectCodeAndMessage() {
        Exception cause = new RuntimeException("Optimistic lock");
        ConcurrentModificationException e = new ConcurrentModificationException("EmailAccount", "acc-123", cause);
        assertThat(e.getErrorCode()).isEqualTo("OPTIMISTIC_LOCK_CONFLICT");
        assertThat(e.getMessage()).contains("EmailAccount");
        assertThat(e.getMessage()).contains("acc-123");
        assertThat(e.getCause()).isSameAs(cause);
    }
}
