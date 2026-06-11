package com.jobtracker.emailmanagement.domain.model;

import java.time.Instant;
import java.util.Objects;


public record OAuthTokenPair(
        String encryptedAccessToken,
        String encryptedRefreshToken,
        Instant tokenExpiry
) {
    public OAuthTokenPair {
        Objects.requireNonNull(encryptedAccessToken,  "encryptedAccessToken must not be null");
        Objects.requireNonNull(encryptedRefreshToken, "encryptedRefreshToken must not be null");
        Objects.requireNonNull(tokenExpiry,           "tokenExpiry must not be null");
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return !now.isBefore(tokenExpiry);
    }
}