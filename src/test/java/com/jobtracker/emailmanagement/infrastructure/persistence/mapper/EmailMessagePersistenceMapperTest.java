package com.jobtracker.emailmanagement.infrastructure.persistence.mapper;

import com.jobtracker.emailmanagement.domain.model.*;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailAttachmentMetadataJpaEntity;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailMessageJpaEntity;
import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailMessagePersistenceMapperTest {

    private final EmailMessagePersistenceMapper mapper = new EmailMessagePersistenceMapper();

    @Test
    void toDomain_mapsAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        EmailMessageJpaEntity entity = new EmailMessageJpaEntity();
        entity.setId(id);
        entity.setGmailMessageId("gmail123");
        entity.setGmailThreadId("thread456");
        entity.setSubject("Interview Invitation");
        entity.setBodyText("Body");
        entity.setBodyHtml("<p>Body</p>");
        entity.setSenderEmail("recruiter@acme.com");
        entity.setRecipients(List.of("user@gmail.com"));
        entity.setCompanyDomain("acme.com");
        entity.setDirection("INBOUND");
        entity.setClassification("APPLICATION");
        entity.setClassificationResult("{\"type\":\"application\"}");
        entity.setClassificationScore(0.95);
        entity.setClassificationConfidence("HIGH");
        entity.setProcessed(true);
        entity.setSentAt(Instant.now());
        entity.setEmailAccountId(UUID.randomUUID());
        entity.setApplicationThreadId(UUID.randomUUID());
        entity.setVersion(1L);

        EmailMessage result = mapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getGmailMessageId()).isEqualTo("gmail123");
        assertThat(result.getSubject()).isEqualTo("Interview Invitation");
        assertThat(result.getDirection()).isEqualTo(EmailDirection.INBOUND);
        assertThat(result.getClassification()).isEqualTo("APPLICATION");
        assertThat(result.getClassificationResult()).isEqualTo("{\"type\":\"application\"}");
        assertThat(result.getClassificationScore()).isEqualTo(0.95);
        assertThat(result.getClassificationConfidence()).isEqualTo("HIGH");
        assertThat(result.isProcessed()).isTrue();
    }

    @Test
    void toDomain_mapsNullClassificationCorrectly() {
        EmailMessageJpaEntity entity = new EmailMessageJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setGmailMessageId("gmail456");
        entity.setGmailThreadId("thread789");
        entity.setSubject("No Class");
        entity.setSenderEmail("x@y.com");
        entity.setRecipients(List.of("user@gmail.com"));
        entity.setCompanyDomain("y.com");
        entity.setDirection("INBOUND");
        entity.setSentAt(Instant.now());
        entity.setEmailAccountId(UUID.randomUUID());
        entity.setVersion(0L);

        EmailMessage result = mapper.toDomain(entity);

        assertThat(result.getClassification()).isNull();
        assertThat(result.getClassificationResult()).isNull();
        assertThat(result.isProcessed()).isFalse();
    }

    @Test
    void roundTrip_preservesAllFields() {
        ClassificationResult cr = new ClassificationResult(
                "APPLICATION", "{\"id\":123}", 0.92, "HIGH");
        EmailMessage original = new EmailMessage.Builder()
                .id(UUID.randomUUID())
                .gmailMessageId("g789")
                .gmailThreadId("t012")
                .emailAccountId(UUID.randomUUID())
                .sender(new EmailAddress("sender@acme.com"))
                .recipients(List.of(new EmailAddress("user@gmail.com")))
                .companyDomain(new CompanyDomain("acme.com"))
                .direction(EmailDirection.INBOUND)
                .sentAt(Instant.now())
                .subject("Round Trip")
                .bodyText("Body")
                .bodyHtml("<p>Body</p>")
                .attachments(List.of())
                .classificationResult(cr)
                .processed(true)
                .applicationThreadId(UUID.randomUUID())
                .version(2L)
                .build();

        EmailMessageJpaEntity entity = mapper.toEntity(original);
        EmailMessage restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getGmailMessageId()).isEqualTo(original.getGmailMessageId());
        assertThat(restored.getClassification()).isEqualTo(original.getClassification());
        assertThat(restored.getClassificationResult()).isEqualTo(original.getClassificationResult());
        assertThat(restored.getClassificationScore()).isEqualTo(original.getClassificationScore());
        assertThat(restored.getClassificationConfidence()).isEqualTo(original.getClassificationConfidence());
        assertThat(restored.isProcessed()).isEqualTo(original.isProcessed());
    }

    @Test
    void toDomain_nullRecipients_returnsEmptyList() {
        EmailMessageJpaEntity entity = new EmailMessageJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setGmailMessageId("g123");
        entity.setGmailThreadId("t456");
        entity.setSubject("No Recip");
        entity.setSenderEmail("s@x.com");
        entity.setCompanyDomain("x.com");
        entity.setDirection("INBOUND");
        entity.setSentAt(Instant.now());
        entity.setEmailAccountId(UUID.randomUUID());
        entity.setVersion(0L);

        EmailMessage result = mapper.toDomain(entity);

        assertThat(result.getRecipients()).isEmpty();
    }

    @Test
    void toEntity_nullClassification_omitsClassificationFields() {
        EmailMessage domain = new EmailMessage.Builder()
                .id(UUID.randomUUID())
                .gmailMessageId("g123")
                .gmailThreadId("t456")
                .emailAccountId(UUID.randomUUID())
                .sender(new EmailAddress("s@x.com"))
                .recipients(List.of(new EmailAddress("r@x.com")))
                .companyDomain(new CompanyDomain("x.com"))
                .direction(EmailDirection.INBOUND)
                .sentAt(Instant.now())
                .subject("Test")
                .bodyText("body")
                .attachments(List.of())
                .build();

        EmailMessageJpaEntity entity = mapper.toEntity(domain);

        assertThat(entity.getClassification()).isNull();
        assertThat(entity.getClassificationResult()).isNull();
        assertThat(entity.getClassificationScore()).isNull();
        assertThat(entity.getClassificationConfidence()).isNull();
    }

    @Test
    void toEntity_withAttachments_mapsCorrectly() {
        List<EmailAttachmentMetadata> attachments = List.of(
                new EmailAttachmentMetadata("resume.pdf", "application/pdf", 1024, "att1"),
                new EmailAttachmentMetadata("letter.doc", "application/msword", 2048, null));
        EmailMessage domain = new EmailMessage.Builder()
                .id(UUID.randomUUID())
                .gmailMessageId("g789")
                .gmailThreadId("t012")
                .emailAccountId(UUID.randomUUID())
                .sender(new EmailAddress("s@x.com"))
                .recipients(List.of(new EmailAddress("r@x.com")))
                .companyDomain(new CompanyDomain("x.com"))
                .direction(EmailDirection.INBOUND)
                .sentAt(Instant.now())
                .subject("With Attachments")
                .bodyText("body")
                .attachments(attachments)
                .build();

        EmailMessageJpaEntity entity = mapper.toEntity(domain);

        assertThat(entity.getAttachments()).hasSize(2);
        assertThat(entity.getAttachments().get(0).getFilename()).isEqualTo("resume.pdf");
        assertThat(entity.getAttachments().get(0).getMimeType()).isEqualTo("application/pdf");
        assertThat(entity.getAttachments().get(0).getSizeBytes()).isEqualTo(1024);
        assertThat(entity.getAttachments().get(0).getGmailAttachmentId()).isEqualTo("att1");
        assertThat(entity.getAttachments().get(1).getFilename()).isEqualTo("letter.doc");
        assertThat(entity.getAttachments().get(1).getMimeType()).isEqualTo("application/msword");
        assertThat(entity.getAttachments().get(1).getSizeBytes()).isEqualTo(2048);
        assertThat(entity.getAttachments().get(1).getGmailAttachmentId()).isNull();
    }

    @Test
    void toDomain_withAttachments_mapsCorrectly() {
        EmailMessageJpaEntity entity = new EmailMessageJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setGmailMessageId("g101");
        entity.setGmailThreadId("t112");
        entity.setSubject("With Attach");
        entity.setSenderEmail("s@x.com");
        entity.setRecipients(List.of("r@x.com"));
        entity.setCompanyDomain("x.com");
        entity.setDirection("INBOUND");
        entity.setSentAt(Instant.now());
        entity.setEmailAccountId(UUID.randomUUID());
        entity.setVersion(0L);

        EmailAttachmentMetadataJpaEntity attEntity = new EmailAttachmentMetadataJpaEntity();
        attEntity.setFilename("photo.jpg");
        attEntity.setMimeType("image/jpeg");
        attEntity.setSizeBytes(5000);
        attEntity.setGmailAttachmentId("att99");
        entity.setAttachments(List.of(attEntity));

        EmailMessage result = mapper.toDomain(entity);

        assertThat(result.getAttachments()).hasSize(1);
        assertThat(result.getAttachments().get(0).filename()).isEqualTo("photo.jpg");
        assertThat(result.getAttachments().get(0).mimeType()).isEqualTo("image/jpeg");
        assertThat(result.getAttachments().get(0).sizeBytes()).isEqualTo(5000);
        assertThat(result.getAttachments().get(0).gmailAttachmentId()).isEqualTo("att99");
    }
}
