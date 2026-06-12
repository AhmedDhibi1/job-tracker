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
    private final UUID id = UUID.randomUUID();

    @Test
    void create_setsActiveAndZeroVersion() {
        EmailAccount account = EmailAccount.create(id, email, "Test User", false, tokens);
        assertThat(account.isActive()).isTrue();
        assertThat(account.getVersion()).isZero();
        assertThat(account.getId()).isEqualTo(id);
        assertThat(account.getEmailAddress()).isEqualTo(email);
        assertThat(account.getDisplayName()).isEqualTo("Test User");
        assertThat(account.getOauthTokens()).isEqualTo(tokens);
        assertThat(account.getSyncState()).isNotNull();
        assertThat(account.getEmptyPollCount()).isZero();
        assertThat(account.isPrimary()).isFalse();
    }

    @Test
    void create_primaryAccount() {
        EmailAccount account = EmailAccount.create(id, email, "Primary User", true, tokens);
        assertThat(account.isPrimary()).isTrue();
    }

    @Test
    void reconstitute_restoresAllFields() {
        SyncState syncState = new SyncState("h1", Instant.now().plusSeconds(3600), true);
        EmailAccount account = EmailAccount.reconstitute(id, email, "Recon User", true, tokens, syncState, 5, false, 3L);
        assertThat(account.getId()).isEqualTo(id);
        assertThat(account.getEmailAddress()).isEqualTo(email);
        assertThat(account.getDisplayName()).isEqualTo("Recon User");
        assertThat(account.isPrimary()).isTrue();
        assertThat(account.getOauthTokens()).isEqualTo(tokens);
        assertThat(account.getSyncState()).isEqualTo(syncState);
        assertThat(account.getEmptyPollCount()).isEqualTo(5);
        assertThat(account.isActive()).isFalse();
        assertThat(account.getVersion()).isEqualTo(3L);
    }

    @Test
    void constructor_rejectsNullId() {
        assertThatThrownBy(() -> new EmailAccount(null, email, "Name", false, tokens, SyncState.initial(), 0, true, 0L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id");
    }

    @Test
    void constructor_rejectsNullEmailAddress() {
        assertThatThrownBy(() -> new EmailAccount(id, null, "Name", false, tokens, SyncState.initial(), 0, true, 0L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("emailAddress");
    }

    @Test
    void constructor_rejectsNullTokens() {
        assertThatThrownBy(() -> new EmailAccount(id, email, "Name", false, null, SyncState.initial(), 0, true, 0L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("oauthTokens");
    }

    @Test
    void constructor_rejectsNullSyncState() {
        assertThatThrownBy(() -> new EmailAccount(id, email, "Name", false, tokens, null, 0, true, 0L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("syncState");
    }

    @Test
    void rejectsNullDisplayName() {
        assertThatThrownBy(() -> EmailAccount.create(id, email, null, false, tokens))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("displayName");
    }

    @Test
    void rejectsBlankDisplayName() {
        assertThatThrownBy(() -> EmailAccount.create(id, email, "   ", false, tokens))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("displayName");
    }

    @Test
    void deactivate_setsActiveToFalse() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        account.deactivate();
        assertThat(account.isActive()).isFalse();
    }

    @Test
    void deactivate_throws_whenAlreadyDeactivated() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        account.deactivate();
        assertThatThrownBy(account::deactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already deactivated");
    }

    @Test
    void reactivate_setsActiveToTrue() {
        EmailAccount account = EmailAccount.reconstitute(id, email, "Test", false, tokens, SyncState.initial(), 0, false, 0L);
        account.reactivate();
        assertThat(account.isActive()).isTrue();
    }

    @Test
    void reactivate_throws_whenAlreadyActive() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        assertThatThrownBy(account::reactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already active");
    }

    @Test
    void updateTokens_succeeds_whenActive() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        OAuthTokenPair newTokens = new OAuthTokenPair("newAccess", "newRefresh", Instant.now().plusSeconds(7200));
        account.updateTokens(newTokens);
        assertThat(account.getOauthTokens()).isEqualTo(newTokens);
    }

    @Test
    void updateTokens_throws_whenDeactivated() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        account.deactivate();
        assertThatThrownBy(() -> account.updateTokens(tokens))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deactivated");
    }

    @Test
    void updateTokens_rejectsNull() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        assertThatThrownBy(() -> account.updateTokens(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("newTokens");
    }

    @Test
    void updateSyncState_updatesState() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        SyncState newState = new SyncState("h2", null, false);
        account.updateSyncState(newState);
        assertThat(account.getSyncState()).isEqualTo(newState);
    }

    @Test
    void updateSyncState_rejectsNull() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        assertThatThrownBy(() -> account.updateSyncState(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("newState");
    }

    @Test
    void incrementEmptyPollCount_incrementsByOne() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        account.incrementEmptyPollCount();
        assertThat(account.getEmptyPollCount()).isEqualTo(1);
        account.incrementEmptyPollCount();
        assertThat(account.getEmptyPollCount()).isEqualTo(2);
    }

    @Test
    void resetEmptyPollCount_resetsToZero() {
        EmailAccount account = EmailAccount.reconstitute(id, email, "Test", false, tokens, SyncState.initial(), 5, true, 0L);
        account.resetEmptyPollCount();
        assertThat(account.getEmptyPollCount()).isZero();
    }

    @Test
    void equals_returnsTrueForSameId() {
        EmailAccount a1 = EmailAccount.create(id, email, "A", false, tokens);
        EmailAccount a2 = EmailAccount.reconstitute(id, email, "B", true, tokens, SyncState.initial(), 0, false, 1L);
        assertThat(a1).isEqualTo(a2);
    }

    @Test
    void equals_returnsFalseForDifferentId() {
        EmailAccount a1 = EmailAccount.create(id, email, "A", false, tokens);
        EmailAccount a2 = EmailAccount.create(UUID.randomUUID(), new EmailAddress("other@x.com"), "B", false, tokens);
        assertThat(a1).isNotEqualTo(a2);
    }

    @Test
    void equals_returnsFalseForDifferentType() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        assertThat(account).isNotEqualTo("string");
    }

    @Test
    void equals_returnsTrueForSameReference() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        assertThat(account).isEqualTo(account);
    }

    @Test
    void hashCode_isConsistentWithId() {
        EmailAccount account = EmailAccount.create(id, email, "Test", false, tokens);
        assertThat(account.hashCode()).isEqualTo(id.hashCode());
    }
}
