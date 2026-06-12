package com.jobtracker.emailmanagement.application.dto;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record FetchedEmailData(
        String gmailMessageId,
        String gmailThreadId,
        Map<String, String> headers,
        List<AttachmentPart> attachments,
        Instant sentAt,
        String from,
        List<String> to,
        String subject,
        String bodyText,
        String bodyHtml,
        List<String> accountEmailAddresses
) {
    public FetchedEmailData {
        Objects.requireNonNull(gmailMessageId);
        Objects.requireNonNull(gmailThreadId);
        headers = headers == null ? Collections.emptyMap() : Map.copyOf(headers);
        attachments = attachments == null ? Collections.emptyList() : List.copyOf(attachments);
        Objects.requireNonNull(sentAt);
        Objects.requireNonNull(from);
        to = to == null ? Collections.emptyList() : List.copyOf(to);
        Objects.requireNonNull(subject);
        accountEmailAddresses = accountEmailAddresses == null
                ? Collections.emptyList() : List.copyOf(accountEmailAddresses);
    }

    public record AttachmentPart(
            String mimeType,
            String contentDisposition,
            String filename,
            String body,
            String gmailAttachmentId,
            long sizeBytes
    ) {
        public AttachmentPart {
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
