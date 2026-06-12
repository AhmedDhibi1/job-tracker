package com.jobtracker.emailmanagement.application.command;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterEmailAccountCommandTest {

    @Test
    void rejectsNullEmailAddress() {
        assertThatThrownBy(() -> new RegisterEmailAccountCommand(null, "User", false, "access", "refresh", Instant.now().plusSeconds(3600)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("emailAddress");
    }

    @Test
    void rejectsBlankEmailAddress() {
        assertThatThrownBy(() -> new RegisterEmailAccountCommand("   ", "User", false, "access", "refresh", Instant.now().plusSeconds(3600)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("emailAddress");
    }

    @Test
    void rejectsNullEncryptedAccessToken() {
        assertThatThrownBy(() -> new RegisterEmailAccountCommand("test@x.com", "User", false, null, "refresh", Instant.now().plusSeconds(3600)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("encryptedAccessToken");
    }

    @Test
    void rejectsBlankEncryptedAccessToken() {
        assertThatThrownBy(() -> new RegisterEmailAccountCommand("test@x.com", "User", false, "   ", "refresh", Instant.now().plusSeconds(3600)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("encryptedAccessToken");
    }

    @Test
    void rejectsNullEncryptedRefreshToken() {
        assertThatThrownBy(() -> new RegisterEmailAccountCommand("test@x.com", "User", false, "access", null, Instant.now().plusSeconds(3600)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("encryptedRefreshToken");
    }

    @Test
    void rejectsBlankEncryptedRefreshToken() {
        assertThatThrownBy(() -> new RegisterEmailAccountCommand("test@x.com", "User", false, "access", "   ", Instant.now().plusSeconds(3600)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("encryptedRefreshToken");
    }

    @Test
    void rejectsNullTokenExpiry() {
        assertThatThrownBy(() -> new RegisterEmailAccountCommand("test@x.com", "User", false, "access", "refresh", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tokenExpiry");
    }

    @Test
    void preservesValues() {
        Instant expiry = Instant.now().plusSeconds(3600);
        RegisterEmailAccountCommand cmd = new RegisterEmailAccountCommand("test@example.com", "Test User", true, "encAccess", "encRefresh", expiry);
        assertThat(cmd.emailAddress()).isEqualTo("test@example.com");
        assertThat(cmd.displayName()).isEqualTo("Test User");
        assertThat(cmd.isPrimary()).isTrue();
        assertThat(cmd.encryptedAccessToken()).isEqualTo("encAccess");
        assertThat(cmd.encryptedRefreshToken()).isEqualTo("encRefresh");
        assertThat(cmd.tokenExpiry()).isEqualTo(expiry);
    }

    @Test
    void toString_redactsTokens() {
        RegisterEmailAccountCommand cmd = new RegisterEmailAccountCommand("test@example.com", "User", false, "secret1", "secret2", Instant.now().plusSeconds(3600));
        String str = cmd.toString();
        assertThat(str).contains("***REDACTED***");
        assertThat(str).doesNotContain("secret1");
        assertThat(str).doesNotContain("secret2");
    }
}
