package com.jobtracker.emailmanagement.application.command;
import java.util.Objects;
import java.util.UUID;

public record InitialSyncCommand(
        UUID emailAccountId,
        int  daysBack
) {
    public InitialSyncCommand {
        Objects.requireNonNull(emailAccountId, "emailAccountId must not be null");
        if (daysBack < 0) throw new IllegalArgumentException("daysBack must be >= 0, got: " + daysBack);
    }
}
