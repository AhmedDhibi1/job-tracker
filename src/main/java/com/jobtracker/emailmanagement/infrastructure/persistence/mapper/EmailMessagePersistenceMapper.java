package com.jobtracker.emailmanagement.infrastructure.persistence.mapper;

import com.jobtracker.emailmanagement.domain.model.*;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailAttachmentMetadataJpaEntity;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailMessageJpaEntity;
import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        ClassificationResult classificationResult = null;
        if (entity.getClassification() != null) {
            classificationResult = new ClassificationResult(
                    entity.getClassification(),
                    entity.getClassificationResult(),
                    entity.getClassificationScore(),
                    entity.getClassificationConfidence()
            );
        }

        return new EmailMessage.Builder()
                .id(entity.getId())
                .gmailMessageId(entity.getGmailMessageId())
                .gmailThreadId(entity.getGmailThreadId())
                .emailAccountId(entity.getEmailAccountId())
                .sender(new EmailAddress(entity.getSenderEmail()))
                .recipients(recipients)
                .companyDomain(new CompanyDomain(entity.getCompanyDomain()))
                .direction(direction)
                .sentAt(entity.getSentAt())
                .subject(entity.getSubject())
                .bodyText(entity.getBodyText())
                .bodyHtml(entity.getBodyHtml())
                .attachments(attachments)
                .classificationResult(classificationResult)
                .processed(entity.isProcessed())
                .applicationThreadId(entity.getApplicationThreadId())
                .version(entity.getVersion())
                .build();
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
            entity.setClassificationResult(domain.getClassificationResult());
            entity.setClassificationScore(domain.getClassificationScore());
            entity.setClassificationConfidence(domain.getClassificationConfidence());
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
