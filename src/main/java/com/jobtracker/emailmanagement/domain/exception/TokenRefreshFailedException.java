package com.jobtracker.emailmanagement.domain.exception;

import com.jobtracker.shared.application.exception.DomainException;
import java.util.UUID;

public class TokenRefreshFailedException extends DomainException {

    public TokenRefreshFailedException(UUID accountId, Throwable cause) {
        super("TOKEN_REFRESH_FAILED",
              "Failed to refresh OAuth2 tokens for account: " + accountId, cause);
    }
}