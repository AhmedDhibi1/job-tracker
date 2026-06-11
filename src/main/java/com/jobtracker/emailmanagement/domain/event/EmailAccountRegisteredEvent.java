package com.jobtracker.emailmanagement.domain.event;

import com.jobtracker.shared.domain.event.DomainEvent;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import java.util.UUID;

public final class EmailAccountRegisteredEvent extends DomainEvent {

    private final UUID accountId;
    private final EmailAddress emailAddress;

    public EmailAccountRegisteredEvent(UUID accountId, EmailAddress emailAddress, String correlationId) {
        super(correlationId);
        this.accountId = accountId;
        this.emailAddress = emailAddress;
    }

    public UUID getAccountId() { return accountId; }
    public EmailAddress getEmailAddress() { return emailAddress; }
}