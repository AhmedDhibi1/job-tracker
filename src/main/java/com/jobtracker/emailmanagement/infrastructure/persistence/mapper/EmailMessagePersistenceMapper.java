package com.jobtracker.emailmanagement.infrastructure.persistence.mapper;

import com.jobtracker.emailmanagement.domain.model.*;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailAttachmentMetadataJpaEntity;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailMessageJpaEntity;
import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class EmailMessagePersistenceMapper {

    public EmailMessage toDomain(EmailMessageJpaEntity entity) {
        List<EmailAddress> recipients = entity.getRecipients() == null
                ? List.of()
                : entity.getRecipients().stream().map(EmailAddress::new).toList();

        List<EmailAttachmentMetadata> attachments = entity.getAttachments() == null
                ? List.of()
                : entity.getAttachments().stream().map(this::toAttachmentDomain).toList();

        EmailDirection direction = entity.getDirection() != null
                ? EmailDirection.valueOf(entity.getDirection())
                : null;

        EmailMessage message = new EmailMessage(
                entity.getId(),
                entity.getGmailMessageId(),
                entity.getGmailThreadId(),
                entity.getEmailAccountId(),
                new EmailAddress(entity.getSenderEmail()),
                recipients,
                new CompanyDomain(entity.getCompanyDomain()),
                direction,
                entity.getSentAt(),
                entity.getSubject(),
                entity.getBodyText(),
                entity.getBodyHtml(),
                attachments,
                entity.getClassification(),
                entity.getClassificationConfidence(),
                entity.isProcessed(),
                entity.getApplicationThreadId(),
                entity.getVersion()
        );

        if (entity.getApplicationThreadId() != null) {
            message.linkToThread(entity.getApplicationThreadId());
        }

        return message;
    }

    public EmailMessageJpaEntity toEntity(EmailMessage domain) {
        EmailMessageJpaEntity entity = new EmailMessageJpaEntity();
        entity.setId(domain.getId());
        entity.setGmailMessageId(domain.getGmailMessageId());
        entity.setGmailThreadId(domain.getGmailThreadId());
        entity.setSubject(domain.getSubject());
        entity.setBodyText(domain.getBodyText());
        entity.setBodyHtml(domain.getBodyHtml());
        entity.setSenderEmail(domain.getSender().value());
        entity.setRecipients(domain.getRecipients().stream().map(EmailAddress::value).toList());
        entity.setCompanyDomain(domain.getCompanyDomain().value());
        entity.setDirection(domain.getDirection() != null ? domain.getDirection().name() : null);
        entity.setProcessed(domain.isProcessed());
        entity.setSentAt(domain.getSentAt());
        entity.setEmailAccountId(domain.getEmailAccountId());
        entity.setApplicationThreadId(domain.getApplicationThreadId());

        if (domain.getClassification() != null) {
            entity.setClassification(domain.getClassification());
            entity.setClassificationConfidence(domain.getClassificationResult());
            entity.setMatchedRuleName(null);
            entity.setMatchedEvidence(new ArrayList<>());
        }

        entity.setAttachments(domain.getAttachments().stream().map(this::toAttachmentEntity).toList());

        return entity;
    }

    private EmailAttachmentMetadata toAttachmentDomain(EmailAttachmentMetadataJpaEntity e) {
        return new EmailAttachmentMetadata(e.getFilename(), e.getMimeType(), e.getSizeBytes(), e.getGmailAttachmentId());
    }

    private EmailAttachmentMetadataJpaEntity toAttachmentEntity(EmailAttachmentMetadata d) {
        EmailAttachmentMetadataJpaEntity e = new EmailAttachmentMetadataJpaEntity();
        e.setFilename(d.filename());
        e.setMimeType(d.mimeType());
        e.setSizeBytes(d.sizeBytes());
        e.setGmailAttachmentId(d.gmailAttachmentId());
        return e;
    }
}
