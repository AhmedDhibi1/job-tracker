package com.jobtracker.emailmanagement.application.command;
import java.time.Instant;
import java.util.Objects;

/**
 * Command to register a new email account.
 * <p>Token values must be pre-encrypted by the caller (adapter/controller layer)
 * before constructing this command, ensuring raw OAuth tokens never cross the
 * application boundary in cleartext.
 */
public record RegisterEmailAccountCommand(
        String emailAddress,
        String displayName,
        boolean isPrimary,
        String encryptedAccessToken,
        String encryptedRefreshToken,
        Instant tokenExpiry
) {
    public RegisterEmailAccountCommand {
        Objects.requireNonNull(emailAddress, "emailAddress must not be null");
        Objects.requireNonNull(encryptedAccessToken, "encryptedAccessToken must not be null");
        Objects.requireNonNull(encryptedRefreshToken, "encryptedRefreshToken must not be null");
        Objects.requireNonNull(tokenExpiry, "tokenExpiry must not be null");
        if (emailAddress.isBlank()) throw new IllegalArgumentException("emailAddress must not be blank");
        if (encryptedAccessToken.isBlank()) throw new IllegalArgumentException("encryptedAccessToken must not be blank");
        if (encryptedRefreshToken.isBlank()) throw new IllegalArgumentException("encryptedRefreshToken must not be blank");
    }

    @Override
    public String toString() {
        return "RegisterEmailAccountCommand[emailAddress=" + emailAddress +
               ", displayName=" + displayName +
               ", isPrimary=" + isPrimary +
               ", encryptedAccessToken=***REDACTED***, encryptedRefreshToken=***REDACTED***]";
    }
}
