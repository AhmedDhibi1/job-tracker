package com.jobtracker.emailmanagement.domain.model;

import java.util.Objects;

public record EmailAttachmentMetadata(
        String filename,
        String mimeType,
        long sizeBytes,
        String gmailAttachmentId
) {
    public EmailAttachmentMetadata {
        Objects.requireNonNull(filename, "filename must not be null");
        Objects.requireNonNull(mimeType, "mimeType must not be null");
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("sizeBytes must be >= 0, got: " + sizeBytes);
        }
    }

    public boolean hasAttachmentId() {
        return gmailAttachmentId != null && !gmailAttachmentId.isBlank();
    }
}