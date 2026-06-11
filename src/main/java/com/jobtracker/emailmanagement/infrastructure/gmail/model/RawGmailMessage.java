package com.jobtracker.emailmanagement.infrastructure.gmail.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public record RawGmailMessage(
        String gmailMessageId,
        String gmailThreadId,
        Map<String, String> headers,
        List<MimePart> parts,
        Instant sentAt,
        String from,
        List<String> to,
        String subject,
        String bodyText,
        String bodyHtml,
        List<String> accountEmailAddresses
) {
    public RawGmailMessage {
        Objects.requireNonNull(gmailMessageId, "gmailMessageId must not be null");
        Objects.requireNonNull(gmailThreadId, "gmailThreadId must not be null");
        headers = headers == null ? Collections.emptyMap() : Map.copyOf(headers);
        parts = parts == null ? Collections.emptyList() : List.copyOf(parts);
        Objects.requireNonNull(sentAt, "sentAt must not be null");
        Objects.requireNonNull(from, "from must not be null");
        to = to == null ? Collections.emptyList() : List.copyOf(to);
        Objects.requireNonNull(subject, "subject must not be null");
        accountEmailAddresses = accountEmailAddresses == null
                ? Collections.emptyList() : List.copyOf(accountEmailAddresses);
    }

    public record MimePart(
            String mimeType,
            String contentDisposition,
            String filename,
            String body,
            String gmailAttachmentId,
            long sizeBytes
    ) {
        public MimePart {
            Objects.requireNonNull(mimeType, "mimeType must not be null");
            filename = filename != null ? filename : "";
            sizeBytes = Math.max(0, sizeBytes);
        }

        public boolean isAttachment() {
            return gmailAttachmentId != null ||
                   (contentDisposition != null && contentDisposition.toLowerCase().contains("attachment"));
        }
    }
}