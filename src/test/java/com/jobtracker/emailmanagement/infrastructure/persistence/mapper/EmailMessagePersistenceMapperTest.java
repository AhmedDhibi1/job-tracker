package com.jobtracker.emailmanagement.infrastructure.persistence.mapper;

import com.jobtracker.emailmanagement.domain.model.*;
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
}
