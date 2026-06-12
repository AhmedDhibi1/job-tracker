package com.jobtracker.emailmanagement.domain.exception;

import com.jobtracker.shared.application.exception.DomainException;

public class PrimaryAccountAlreadyExistsException extends DomainException {

    public PrimaryAccountAlreadyExistsException(String existingAccountId) {
        super("PRIMARY_ACCOUNT_EXISTS",
              "A primary email account already exists: " + existingAccountId);
    }
}
