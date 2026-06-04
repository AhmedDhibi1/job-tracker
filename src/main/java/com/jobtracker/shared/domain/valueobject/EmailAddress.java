package com.jobtracker.shared.domain.valueobject;

import java.util.regex.Pattern;


public record EmailAddress(String value) {

    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$"
    );

    
    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Email address must not be null or blank"
            );
        }
        value = value.strip().toLowerCase();
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Invalid email address format: '" + value + "'"
            );
        }
    }

    
    public String domain() {
        return value.substring(value.indexOf('@') + 1);
    }

    
    @Override
    public String toString() {
        return value;
    }
}