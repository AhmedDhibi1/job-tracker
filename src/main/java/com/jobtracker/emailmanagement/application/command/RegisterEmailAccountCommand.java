package com.jobtracker.emailmanagement.application.command;
import java.time.Instant;
import java.util.Objects;

public record RegisterEmailAccountCommand(
        String emailAddress,
        String displayName,
        boolean isPrimary,
        String rawAccessToken,
        String rawRefreshToken,
        Instant tokenExpiry
) {
    public RegisterEmailAccountCommand {
        Objects.requireNonNull(emailAddress, "emailAddress must not be null");
        Objects.requireNonNull(rawAccessToken, "rawAccessToken must not be null");
        Objects.requireNonNull(rawRefreshToken, "rawRefreshToken must not be null");
        Objects.requireNonNull(tokenExpiry, "tokenExpiry must not be null");
        if (emailAddress.isBlank()) throw new IllegalArgumentException("emailAddress must not be blank");
        if (rawAccessToken.isBlank()) throw new IllegalArgumentException("rawAccessToken must not be blank");
        if (rawRefreshToken.isBlank()) throw new IllegalArgumentException("rawRefreshToken must not be blank");
    }

    @Override
    public String toString() {
        return "RegisterEmailAccountCommand[emailAddress=" + emailAddress +
               ", displayName=" + displayName +
               ", isPrimary=" + isPrimary +
               ", rawAccessToken=***REDACTED***, rawRefreshToken=***REDACTED***]";
    }
}
