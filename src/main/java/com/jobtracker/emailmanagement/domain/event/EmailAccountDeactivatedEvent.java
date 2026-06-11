package com.jobtracker.emailmanagement.domain.event;

import com.jobtracker.shared.domain.event.DomainEvent;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import java.util.UUID;

public final class EmailAccountDeactivatedEvent extends DomainEvent {

    private final UUID accountId;
    private final EmailAddress emailAddress;
    private final String reason;

    public EmailAccountDeactivatedEvent(UUID accountId, EmailAddress emailAddress, String reason, String correlationId) {
        super(correlationId);
        this.accountId = accountId;
        this.emailAddress = emailAddress;
        this.reason = reason;
    }

    public UUID getAccountId() { return accountId; }
    public EmailAddress getEmailAddress() { return emailAddress; }
    public String getReason() { return reason; }
}
