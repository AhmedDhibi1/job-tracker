package com.jobtracker.shared.domain.valueobject;

import java.util.Objects;


public final class CompanyDomain {

    private final String value;

    
    public CompanyDomain(String rawDomain) {
        if (rawDomain == null || rawDomain.isBlank()) {
            throw new IllegalArgumentException(
                    "Company domain must not be null or blank"
            );
        }
        String normalized = rawDomain.strip().toLowerCase();
        if (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    "Company domain must not be blank after stripping '@' prefix"
            );
        }
        this.value = normalized;
    }

    
    public static CompanyDomain from(EmailAddress emailAddress) {
        Objects.requireNonNull(emailAddress, "EmailAddress must not be null");
        return new CompanyDomain(emailAddress.domain());
    }

    
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyDomain that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}