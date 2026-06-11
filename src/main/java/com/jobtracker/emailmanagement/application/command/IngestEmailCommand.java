package com.jobtracker.emailmanagement.application.command;
import java.util.Objects;
import java.util.UUID;

public record IngestEmailCommand(
        UUID   emailAccountId,
        String gmailMessageId,
        String correlationId
) {
    public IngestEmailCommand {
        Objects.requireNonNull(emailAccountId, "emailAccountId must not be null");
        Objects.requireNonNull(gmailMessageId, "gmailMessageId must not be null");
        if (gmailMessageId.isBlank()) throw new IllegalArgumentException("gmailMessageId must not be blank");
    }
}
