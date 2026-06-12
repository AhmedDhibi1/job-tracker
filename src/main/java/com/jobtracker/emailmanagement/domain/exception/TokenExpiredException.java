package com.jobtracker.emailmanagement.domain.exception;

import com.jobtracker.shared.application.exception.DomainException;
import java.util.UUID;

public class TokenExpiredException extends DomainException {

    public TokenExpiredException(UUID accountId) {
        super("TOKEN_EXPIRED", "Access token has expired for account: " + accountId);
    }
}
