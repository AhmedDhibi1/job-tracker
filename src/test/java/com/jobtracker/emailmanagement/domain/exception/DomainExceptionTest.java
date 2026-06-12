package com.jobtracker.emailmanagement.domain.exception;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void tokenExpiredException_hasCorrectCodeAndMessage() {
        UUID id = UUID.randomUUID();
        TokenExpiredException e = new TokenExpiredException(id);
        assertThat(e.getErrorCode()).isEqualTo("TOKEN_EXPIRED");
        assertThat(e.getMessage()).contains(id.toString());
    }

    @Test
    void primaryAccountAlreadyExistsException_hasCorrectCodeAndMessage() {
        PrimaryAccountAlreadyExistsException e = new PrimaryAccountAlreadyExistsException("acc-123");
        assertThat(e.getErrorCode()).isEqualTo("PRIMARY_ACCOUNT_EXISTS");
        assertThat(e.getMessage()).contains("acc-123");
    }

    @Test
    void decryptionFailedException_hasCorrectCodeAndMessageAndCause() {
        Exception cause = new RuntimeException("crypto error");
        DecryptionFailedException e = new DecryptionFailedException("Failed to decrypt token", cause);
        assertThat(e.getErrorCode()).isEqualTo("DECRYPTION_FAILED");
        assertThat(e.getMessage()).contains("Failed to decrypt token");
        assertThat(e.getCause()).isSameAs(cause);
    }

    @Test
    void tokenRefreshFailedException_hasCorrectCodeAndMessageAndCause() {
        UUID id = UUID.randomUUID();
        Exception cause = new RuntimeException("network error");
        TokenRefreshFailedException e = new TokenRefreshFailedException(id, cause);
        assertThat(e.getErrorCode()).isEqualTo("TOKEN_REFRESH_FAILED");
        assertThat(e.getMessage()).contains(id.toString());
        assertThat(e.getCause()).isSameAs(cause);
    }

    @Test
    void duplicateEmailAccountException_hasCorrectCodeAndMessage() {
        EmailAddress email = new EmailAddress("existing@example.com");
        DuplicateEmailAccountException e = new DuplicateEmailAccountException(email);
        assertThat(e.getErrorCode()).isEqualTo("DUPLICATE_EMAIL_ACCOUNT");
        assertThat(e.getMessage()).contains(email.value());
    }
}
