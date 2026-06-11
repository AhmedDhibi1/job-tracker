package com.jobtracker.emailmanagement.domain.exception;

import com.jobtracker.shared.application.exception.DomainException;

public class DecryptionFailedException extends DomainException {

    public DecryptionFailedException(String message, Throwable cause) {
        super("DECRYPTION_FAILED", message, cause);
    }
}
