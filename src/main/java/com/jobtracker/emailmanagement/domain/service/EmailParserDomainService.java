package com.jobtracker.emailmanagement.domain.service;

import com.jobtracker.emailmanagement.application.dto.FetchedEmailData;
import com.jobtracker.emailmanagement.domain.model.EmailAttachmentMetadata;
import com.jobtracker.emailmanagement.domain.model.EmailDirection;
import com.jobtracker.emailmanagement.domain.model.EmailMessage;
import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import jakarta.mail.Address;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmailParserDomainService {

    public EmailMessage parse(
            FetchedEmailData raw,
            UUID emailAccountId,
            EmailAddress accountAddress) {

        EmailAddress sender            = extractSender(raw);
        List<EmailAddress> recipients  = extractRecipients(raw);
        EmailDirection direction       = determineDirection(sender, accountAddress);
        CompanyDomain companyDomain    = deriveCompanyDomain(sender, recipients, direction, accountAddress);
        String subject                 = extractHeader(raw, "Subject");
        String bodyText                = extractBodyText(raw);
        String bodyHtml                = extractBodyHtml(raw);
        List<EmailAttachmentMetadata> attachments = extractAttachments(raw);

        return EmailMessage.create(
                UUID.randomUUID(),
                raw.gmailMessageId(),
                raw.gmailThreadId(),
                emailAccountId,
                sender, recipients, companyDomain, direction,
                raw.sentAt(),
                subject, bodyText, bodyHtml, attachments);
    }


    private EmailAddress extractSender(FetchedEmailData raw) {
        String fromHeader = raw.headers().get("from");
        if (fromHeader == null || fromHeader.isBlank()) {
            throw new IllegalArgumentException("Missing 'from' header in message " + raw.gmailMessageId());
        }
        try {
            InternetAddress[] addresses = InternetAddress.parse(fromHeader);
            return new EmailAddress(addresses[0].getAddress());
        } catch (AddressException e) {
            throw new IllegalArgumentException("Cannot parse 'from' header: " + fromHeader, e);
        }
    }

    private List<EmailAddress> extractRecipients(FetchedEmailData raw) {
        List<EmailAddress> recipients = new ArrayList<>();
        for (String header : List.of("to", "cc", "bcc")) {
            String value = raw.headers().get(header);
            if (value != null && !value.isBlank()) {
                try {
                    Address[] addresses = InternetAddress.parse(value);
                    for (Address addr : addresses) {
                        if (addr instanceof InternetAddress ia && ia.getAddress() != null) {
                            recipients.add(new EmailAddress(ia.getAddress()));
                        }
                    }
                } catch (AddressException e) {
                    for (String part : value.split(",")) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty()) {
                            recipients.add(new EmailAddress(trimmed));
                        }
                    }
                }
            }
        }
        return recipients;
    }

    private String extractHeader(FetchedEmailData raw, String headerName) {
        return raw.headers().getOrDefault(headerName.toLowerCase(), "");
    }

    private String extractBodyText(FetchedEmailData raw) {
        if (raw.bodyText() != null && !raw.bodyText().isBlank()) {
            return raw.bodyText();
        }
        for (FetchedEmailData.AttachmentPart part : raw.attachments()) {
            if ("text/plain".equalsIgnoreCase(part.mimeType()) && part.body() != null) {
                return part.body();
            }
        }
        return "";
    }

    private String extractBodyHtml(FetchedEmailData raw) {
        if (raw.bodyHtml() != null && !raw.bodyHtml().isBlank()) {
            return raw.bodyHtml();
        }
        for (FetchedEmailData.AttachmentPart part : raw.attachments()) {
            if ("text/html".equalsIgnoreCase(part.mimeType()) && part.body() != null) {
                return part.body();
            }
        }
        return "";
    }

    private List<EmailAttachmentMetadata> extractAttachments(FetchedEmailData raw) {
        List<EmailAttachmentMetadata> attachments = new ArrayList<>();
        for (FetchedEmailData.AttachmentPart part : raw.attachments()) {
            if (part.isAttachment()) {
                attachments.add(new EmailAttachmentMetadata(
                        part.filename(),
                        part.mimeType(),
                        part.sizeBytes(),
                        part.gmailAttachmentId()
                ));
            }
        }
        return attachments;
    }

    private EmailDirection determineDirection(EmailAddress sender, EmailAddress account) {
        return sender.equals(account)
                ? EmailDirection.OUTBOUND
                : EmailDirection.INBOUND;
    }

    private CompanyDomain deriveCompanyDomain(
            EmailAddress sender,
            List<EmailAddress> recipients,
            EmailDirection direction,
            EmailAddress accountAddress) {
        if (direction == EmailDirection.INBOUND) {
            return CompanyDomain.from(sender);
        }
        return recipients.stream()
                .filter(r -> !r.domain().equalsIgnoreCase(accountAddress.domain()))
                .findFirst()
                .map(CompanyDomain::from)
                .orElse(CompanyDomain.from(sender));
    }
}
