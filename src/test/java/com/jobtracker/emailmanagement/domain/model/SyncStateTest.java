package com.jobtracker.emailmanagement.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncStateTest {

    @Test
    void initial_hasAllNullsAndPushDisabled() {
        SyncState state = SyncState.initial();
        assertThat(state.historyId()).isNull();
        assertThat(state.watchExpiration()).isNull();
        assertThat(state.pushEnabled()).isFalse();
    }

    @Test
    void isWatchExpired_returnsFalseWhenNoExpiration() {
        SyncState state = SyncState.initial();
        assertThat(state.isWatchExpired(Instant.now())).isFalse();
    }

    @Test
    void isWatchExpired_returnsTrueWhenExpired() {
        SyncState state = new SyncState("h1", Instant.now().minusSeconds(60), true);
        assertThat(state.isWatchExpired(Instant.now())).isTrue();
    }

    @Test
    void isWatchExpired_returnsFalseWhenNotExpired() {
        SyncState state = new SyncState("h1", Instant.now().plusSeconds(60), true);
        assertThat(state.isWatchExpired(Instant.now())).isFalse();
    }

    @Test
    void isWatchExpired_returnsTrueAtExactExpiration() {
        Instant now = Instant.now();
        SyncState state = new SyncState("h1", now, true);
        assertThat(state.isWatchExpired(now)).isTrue();
    }

    @Test
    void isWatchExpired_rejectsNullNow() {
        SyncState state = SyncState.initial();
        assertThatThrownBy(() -> state.isWatchExpired(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("now");
    }

    @Test
    void withUpdatedHistoryId_preservesWatchAndPush() {
        SyncState state = new SyncState("oldId", Instant.now().plusSeconds(3600), true);
        SyncState updated = state.withUpdatedHistoryId("newId");
        assertThat(updated.historyId()).isEqualTo("newId");
        assertThat(updated.watchExpiration()).isEqualTo(state.watchExpiration());
        assertThat(updated.pushEnabled()).isTrue();
    }

    @Test
    void withWatchRegistered_setsExpirationAndEnablesPush() {
        SyncState state = SyncState.initial();
        Instant expiration = Instant.now().plusSeconds(3600);
        SyncState registered = state.withWatchRegistered(expiration);
        assertThat(registered.watchExpiration()).isEqualTo(expiration);
        assertThat(registered.pushEnabled()).isTrue();
        assertThat(registered.historyId()).isNull();
    }

    @Test
    void withWatchRegistered_rejectsNullExpiration() {
        SyncState state = SyncState.initial();
        assertThatThrownBy(() -> state.withWatchRegistered(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("expiration");
    }

    @Test
    void withWatchStopped_clearsExpirationAndDisablesPush() {
        SyncState state = new SyncState("h1", Instant.now().plusSeconds(3600), true);
        SyncState stopped = state.withWatchStopped();
        assertThat(stopped.watchExpiration()).isNull();
        assertThat(stopped.pushEnabled()).isFalse();
        assertThat(stopped.historyId()).isEqualTo("h1");
    }

    @Test
    void equalityBasedOnAllComponents() {
        Instant exp = Instant.now().plusSeconds(3600);
        SyncState s1 = new SyncState("h1", exp, true);
        SyncState s2 = new SyncState("h1", exp, true);
        assertThat(s1).isEqualTo(s2);
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    }
}
