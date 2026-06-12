package com.jobtracker.shared.application.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void domainException_withMessage() {
        DomainException e = new DomainException("ERROR_CODE", "Something went wrong") {};
        assertThat(e.getErrorCode()).isEqualTo("ERROR_CODE");
        assertThat(e.getMessage()).isEqualTo("Something went wrong");
        assertThat(e.getCause()).isNull();
    }

    @Test
    void domainException_withMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        DomainException e = new DomainException("ERROR_CODE", "Something went wrong", cause) {};
        assertThat(e.getErrorCode()).isEqualTo("ERROR_CODE");
        assertThat(e.getMessage()).isEqualTo("Something went wrong");
        assertThat(e.getCause()).isSameAs(cause);
    }
}
