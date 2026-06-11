package com.jobtracker.emailmanagement.domain.exception;

import com.jobtracker.shared.application.exception.DomainException;
import com.jobtracker.shared.domain.valueobject.EmailAddress;

public class DuplicateEmailAccountException extends DomainException {

    public DuplicateEmailAccountException(EmailAddress emailAddress) {
        super("DUPLICATE_EMAIL_ACCOUNT",
              "An email account is already registered for: " + emailAddress.value());
    }
}