package com.jobtracker.emailmanagement.domain.model;

import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailMessageTest {

    private final EmailAddress sender = new EmailAddress("sender@acme.com");
    private final List<EmailAddress> recipients = List.of(new EmailAddress("user@gmail.com"));
    private final CompanyDomain domain = new CompanyDomain("acme.com");

    private EmailMessage createMessage() {
        return EmailMessage.create(
                UUID.randomUUID(), "gmail1", "thread1", UUID.randomUUID(),
                sender, recipients, domain, EmailDirection.INBOUND,
                Instant.now(), "Subject", "Body", null, List.of());
    }

    @Test
    void markClassified_setsProcessed() {
        EmailMessage msg = createMessage();
        ClassificationResult result = new ClassificationResult(
                "APPLICATION", "{\"type\":\"application\"}", 0.95, "HIGH");
        msg.markClassified(result);
        assertThat(msg.isProcessed()).isTrue();
        assertThat(msg.getClassification()).isEqualTo("APPLICATION");
        assertThat(msg.getClassificationScore()).isEqualTo(0.95);
    }

    @Test
    void markClassified_throws_whenAlreadyProcessed() {
        EmailMessage msg = createMessage();
        msg.markClassified(new ClassificationResult("SPAM", null, 0.8, "MEDIUM"));
        assertThatThrownBy(() -> msg.markClassified(
                new ClassificationResult("APPLICATION", null, 0.9, "HIGH")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been processed");
    }

    @Test
    void linkToThread_succeeds_whenNotAlreadyLinked() {
        EmailMessage msg = createMessage();
        UUID threadId = UUID.randomUUID();
        msg.linkToThread(threadId);
        assertThat(msg.getApplicationThreadId()).isEqualTo(threadId);
    }

    @Test
    void linkToThread_throws_whenAlreadyLinked() {
        EmailMessage msg = createMessage();
        msg.linkToThread(UUID.randomUUID());
        assertThatThrownBy(() -> msg.linkToThread(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already linked");
    }
}
