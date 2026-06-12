package com.jobtracker.emailmanagement.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthTokenPairTest {

    @Test
    void rejectsPastExpiry() {
        assertThatThrownBy(() -> new OAuthTokenPair("encAccess", "encRefresh", Instant.now().minusSeconds(60)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void acceptsFutureExpiry() {
        OAuthTokenPair pair = new OAuthTokenPair("encAccess", "encRefresh", Instant.now().plusSeconds(3600));
        assertThat(pair.isExpired(Instant.now())).isFalse();
    }

    @Test
    void isExpired_returnsTrue_whenPastExpiry() {
        Instant future = Instant.now().plusSeconds(10);
        OAuthTokenPair pair = new OAuthTokenPair("encAccess", "encRefresh", future);
        assertThat(pair.isExpired(future.plusSeconds(1))).isTrue();
    }

    @Test
    void isExpired_returnsTrue_whenExactlyAtExpiry() {
        Instant future = Instant.now().plusSeconds(10);
        OAuthTokenPair pair = new OAuthTokenPair("encAccess", "encRefresh", future);
        assertThat(pair.isExpired(future)).isTrue();
    }

    @Test
    void rejectsNullEncryptedAccessToken() {
        assertThatThrownBy(() -> new OAuthTokenPair(null, "encRefresh", Instant.now().plusSeconds(3600)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("encryptedAccessToken");
    }

    @Test
    void rejectsNullEncryptedRefreshToken() {
        assertThatThrownBy(() -> new OAuthTokenPair("encAccess", null, Instant.now().plusSeconds(3600)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("encryptedRefreshToken");
    }

    @Test
    void rejectsNullTokenExpiry() {
        assertThatThrownBy(() -> new OAuthTokenPair("encAccess", "encRefresh", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tokenExpiry");
    }

    @Test
    void isExpired_rejectsNullNow() {
        OAuthTokenPair pair = new OAuthTokenPair("encAccess", "encRefresh", Instant.now().plusSeconds(3600));
        assertThatThrownBy(() -> pair.isExpired(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("now");
    }

    @Test
    void preservesValues() {
        Instant expiry = Instant.now().plusSeconds(3600);
        OAuthTokenPair pair = new OAuthTokenPair("access123", "refresh456", expiry);
        assertThat(pair.encryptedAccessToken()).isEqualTo("access123");
        assertThat(pair.encryptedRefreshToken()).isEqualTo("refresh456");
        assertThat(pair.tokenExpiry()).isEqualTo(expiry);
    }
}
