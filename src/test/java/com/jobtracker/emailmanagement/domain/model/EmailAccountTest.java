package com.jobtracker.emailmanagement.domain.model;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAccountTest {

    private final EmailAddress email = new EmailAddress("test@example.com");
    private final OAuthTokenPair tokens = new OAuthTokenPair("encAccess", "encRefresh", Instant.now().plusSeconds(3600));

    @Test
    void create_setsActiveAndZeroVersion() {
        EmailAccount account = EmailAccount.create(UUID.randomUUID(), email, "Test User", false, tokens);
        assertThat(account.isActive()).isTrue();
        assertThat(account.getVersion()).isZero();
    }

    @Test
    void deactivate_throws_whenAlreadyDeactivated() {
        EmailAccount account = EmailAccount.create(UUID.randomUUID(), email, "Test User", false, tokens);
        account.deactivate();
        assertThatThrownBy(account::deactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already deactivated");
    }

    @Test
    void reactivate_throws_whenAlreadyActive() {
        EmailAccount account = EmailAccount.create(UUID.randomUUID(), email, "Test User", false, tokens);
        assertThatThrownBy(account::reactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already active");
    }

    @Test
    void updateTokens_throws_whenDeactivated() {
        EmailAccount account = EmailAccount.create(UUID.randomUUID(), email, "Test User", false, tokens);
        account.deactivate();
        assertThatThrownBy(() -> account.updateTokens(tokens))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deactivated");
    }

    @Test
    void rejectsBlankDisplayName() {
        assertThatThrownBy(() -> EmailAccount.create(
                UUID.randomUUID(), email, "   ", false, tokens))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("displayName");
    }
}
