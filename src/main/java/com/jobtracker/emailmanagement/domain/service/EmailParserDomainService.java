package com.jobtracker.emailmanagement.domain.service;

import com.jobtracker.emailmanagement.domain.model.EmailAttachmentMetadata;
import com.jobtracker.emailmanagement.domain.model.EmailDirection;
import com.jobtracker.emailmanagement.domain.model.EmailMessage;
import com.jobtracker.emailmanagement.domain.model.RawEmailInput;
import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EmailParserDomainService {

    public EmailMessage parse(
            RawEmailInput raw,
            UUID emailAccountId,
            EmailAddress accountAddress) {

        EmailAddress sender            = raw.from();
        List<EmailAddress> recipients  = raw.to();
        EmailDirection direction       = determineDirection(sender, accountAddress);
        CompanyDomain companyDomain    = deriveCompanyDomain(sender, recipients, direction, accountAddress);
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
                raw.subject(), bodyText, bodyHtml, attachments);
    }

    private String extractBodyText(RawEmailInput raw) {
        if (raw.bodyText() != null && !raw.bodyText().isBlank()) {
            return raw.bodyText();
        }
        for (RawEmailInput.PartInput part : raw.parts()) {
            if ("text/plain".equalsIgnoreCase(part.mimeType()) && part.body() != null) {
                return part.body();
            }
        }
        return "";
    }

    private String extractBodyHtml(RawEmailInput raw) {
        if (raw.bodyHtml() != null && !raw.bodyHtml().isBlank()) {
            return raw.bodyHtml();
        }
        for (RawEmailInput.PartInput part : raw.parts()) {
            if ("text/html".equalsIgnoreCase(part.mimeType()) && part.body() != null) {
                return part.body();
            }
        }
        return "";
    }

    private List<EmailAttachmentMetadata> extractAttachments(RawEmailInput raw) {
        List<EmailAttachmentMetadata> attachments = new ArrayList<>();
        for (RawEmailInput.PartInput part : raw.parts()) {
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
