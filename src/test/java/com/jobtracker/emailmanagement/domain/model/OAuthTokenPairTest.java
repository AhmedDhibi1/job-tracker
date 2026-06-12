package com.jobtracker.emailmanagement.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthTokenPairTest {

    @Test
    void rejectsPastExpiry() {
        assertThatThrownBy(() -> new OAuthTokenPair(
                "encAccess", "encRefresh", Instant.now().minusSeconds(60)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void acceptsFutureExpiry() {
        OAuthTokenPair pair = new OAuthTokenPair(
                "encAccess", "encRefresh", Instant.now().plusSeconds(3600));
        assertThat(pair.isExpired(Instant.now())).isFalse();
    }

    @Test
    void isExpired_returnsTrue_whenPastExpiry() {
        Instant future = Instant.now().plusSeconds(10);
        OAuthTokenPair pair = new OAuthTokenPair("encAccess", "encRefresh", future);
        assertThat(pair.isExpired(future.plusSeconds(1))).isTrue();
    }
}
