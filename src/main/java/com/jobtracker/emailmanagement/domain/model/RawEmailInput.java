package com.jobtracker.emailmanagement.domain.model;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record RawEmailInput(
        String gmailMessageId,
        String gmailThreadId,
        Map<String, String> headers,
        List<PartInput> parts,
        Instant sentAt,
        EmailAddress from,
        List<EmailAddress> to,
        String subject,
        String bodyText,
        String bodyHtml
) {
    public RawEmailInput {
        Objects.requireNonNull(gmailMessageId);
        Objects.requireNonNull(gmailThreadId);
        headers = headers == null ? Collections.emptyMap() : Map.copyOf(headers);
        parts = parts == null ? Collections.emptyList() : List.copyOf(parts);
        Objects.requireNonNull(sentAt);
        Objects.requireNonNull(from);
        to = to == null ? Collections.emptyList() : List.copyOf(to);
        Objects.requireNonNull(subject);
    }

    public record PartInput(
            String mimeType,
            String contentDisposition,
            String filename,
            String body,
            String gmailAttachmentId,
            long sizeBytes
    ) {
        public PartInput {
            Objects.requireNonNull(mimeType);
            filename = filename != null ? filename : "";
            sizeBytes = Math.max(0, sizeBytes);
        }

        public boolean isAttachment() {
            return gmailAttachmentId != null ||
                   (contentDisposition != null && contentDisposition.toLowerCase().contains("attachment"));
        }
    }
}
