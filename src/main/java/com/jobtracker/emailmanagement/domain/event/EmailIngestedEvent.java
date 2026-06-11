package com.jobtracker.emailmanagement.domain.event;

import com.jobtracker.shared.domain.event.DomainEvent;
import java.util.UUID;


public final class EmailIngestedEvent extends DomainEvent {

    private final UUID   emailMessageId;
    private final String gmailMessageId;
    private final UUID   accountId;

    public EmailIngestedEvent(UUID emailMessageId, String gmailMessageId,
                              UUID accountId, String correlationId) {
        super(correlationId);
        this.emailMessageId = emailMessageId;
        this.gmailMessageId = gmailMessageId;
        this.accountId = accountId;
    }

    public UUID getEmailMessageId() { return emailMessageId; }
    public String getGmailMessageId() { return gmailMessageId; }
    public UUID getAccountId() { return accountId; }
}