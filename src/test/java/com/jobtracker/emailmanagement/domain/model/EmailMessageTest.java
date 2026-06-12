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

    private final UUID id = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final EmailAddress sender = new EmailAddress("sender@acme.com");
    private final List<EmailAddress> recipients = List.of(new EmailAddress("user@gmail.com"));
    private final CompanyDomain domain = new CompanyDomain("acme.com");
    private final Instant now = Instant.now();

    private EmailMessage createMessage() {
        return EmailMessage.create(
                id, "gmail1", "thread1", accountId,
                sender, recipients, domain, EmailDirection.INBOUND,
                now, "Subject", "Body", null, List.of());
    }

    @Test
    void create_setsFieldsCorrectly() {
        EmailMessage msg = createMessage();
        assertThat(msg.getId()).isEqualTo(id);
        assertThat(msg.getGmailMessageId()).isEqualTo("gmail1");
        assertThat(msg.getGmailThreadId()).isEqualTo("thread1");
        assertThat(msg.getEmailAccountId()).isEqualTo(accountId);
        assertThat(msg.getSender()).isEqualTo(sender);
        assertThat(msg.getRecipients()).containsExactlyElementsOf(recipients);
        assertThat(msg.getCompanyDomain()).isEqualTo(domain);
        assertThat(msg.getDirection()).isEqualTo(EmailDirection.INBOUND);
        assertThat(msg.getSentAt()).isEqualTo(now);
        assertThat(msg.getSubject()).isEqualTo("Subject");
        assertThat(msg.getBodyText()).isEqualTo("Body");
        assertThat(msg.getBodyHtml()).isEmpty();
        assertThat(msg.getAttachments()).isEmpty();
        assertThat(msg.isProcessed()).isFalse();
        assertThat(msg.getClassificationResultObj()).isNull();
        assertThat(msg.getApplicationThreadId()).isNull();
        assertThat(msg.getVersion()).isNull();
    }

    @Test
    void create_defaultsNullBodyToEmpty() {
        EmailMessage msg = EmailMessage.create(id, "g1", "t1", accountId,
                sender, recipients, domain, EmailDirection.INBOUND,
                now, "Subj", null, null, List.of());
        assertThat(msg.getBodyText()).isEmpty();
        assertThat(msg.getBodyHtml()).isEmpty();
    }

    @Test
    void create_defaultsNullSubjectToEmpty() {
        EmailMessage msg = EmailMessage.create(id, "g1", "t1", accountId,
                sender, recipients, domain, EmailDirection.INBOUND,
                now, null, "Body", null, List.of());
        assertThat(msg.getSubject()).isEmpty();
    }

    @Test
    void markClassified_setsProcessedAndResult() {
        EmailMessage msg = createMessage();
        ClassificationResult result = new ClassificationResult("APPLICATION", "{\"type\":\"application\"}", 0.95, "HIGH");
        msg.markClassified(result);
        assertThat(msg.isProcessed()).isTrue();
        assertThat(msg.getClassification()).isEqualTo("APPLICATION");
        assertThat(msg.getClassificationResult()).isEqualTo("{\"type\":\"application\"}");
        assertThat(msg.getClassificationScore()).isEqualTo(0.95);
        assertThat(msg.getClassificationConfidence()).isEqualTo("HIGH");
    }

    @Test
    void markClassified_withNullScore() {
        EmailMessage msg = createMessage();
        ClassificationResult result = new ClassificationResult("SPAM", null, null, null);
        msg.markClassified(result);
        assertThat(msg.isProcessed()).isTrue();
        assertThat(msg.getClassificationScore()).isNull();
        assertThat(msg.getClassificationConfidence()).isNull();
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
    void markClassified_rejectsNullResult() {
        EmailMessage msg = createMessage();
        assertThatThrownBy(() -> msg.markClassified(null))
                .isInstanceOf(NullPointerException.class);
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

    @Test
    void linkToThread_rejectsNullThreadId() {
        EmailMessage msg = createMessage();
        assertThatThrownBy(() -> msg.linkToThread(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("threadId");
    }

    @Test
    void getClassificationReturnsNull_whenNotClassified() {
        EmailMessage msg = createMessage();
        assertThat(msg.getClassification()).isNull();
        assertThat(msg.getClassificationResult()).isNull();
        assertThat(msg.getClassificationScore()).isNull();
        assertThat(msg.getClassificationConfidence()).isNull();
    }

    @Test
    void builder_constructsMessage() {
        UUID threadId = UUID.randomUUID();
        ClassificationResult cr = new ClassificationResult("APPLICATION", "{}", 0.9, "HIGH");
        EmailMessage msg = EmailMessage.builder()
                .id(id)
                .gmailMessageId("g1")
                .gmailThreadId("t1")
                .emailAccountId(accountId)
                .sender(sender)
                .recipients(recipients)
                .companyDomain(domain)
                .direction(EmailDirection.OUTBOUND)
                .sentAt(now)
                .subject("Subj")
                .bodyText("Body")
                .bodyHtml("<p>Body</p>")
                .attachments(List.of())
                .classificationResult(cr)
                .processed(true)
                .applicationThreadId(threadId)
                .version(5L)
                .build();

        assertThat(msg.getId()).isEqualTo(id);
        assertThat(msg.getDirection()).isEqualTo(EmailDirection.OUTBOUND);
        assertThat(msg.getClassification()).isEqualTo("APPLICATION");
        assertThat(msg.isProcessed()).isTrue();
        assertThat(msg.getApplicationThreadId()).isEqualTo(threadId);
        assertThat(msg.getVersion()).isEqualTo(5L);
    }

    @Test
    void constructor_rejectsNullId() {
        assertThatThrownBy(() -> new EmailMessage(null, "g1", "t1", accountId,
                sender, recipients, domain, EmailDirection.INBOUND, now,
                "s", "b", null, List.of(), null, false, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_rejectsNullGmailMessageId() {
        assertThatThrownBy(() -> new EmailMessage(id, null, "t1", accountId,
                sender, recipients, domain, EmailDirection.INBOUND, now,
                "s", "b", null, List.of(), null, false, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_rejectsNullSender() {
        assertThatThrownBy(() -> new EmailMessage(id, "g1", "t1", accountId,
                null, recipients, domain, EmailDirection.INBOUND, now,
                "s", "b", null, List.of(), null, false, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_rejectsNullRecipients() {
        assertThatThrownBy(() -> new EmailMessage(id, "g1", "t1", accountId,
                sender, null, domain, EmailDirection.INBOUND, now,
                "s", "b", null, List.of(), null, false, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_rejectsNullCompanyDomain() {
        assertThatThrownBy(() -> new EmailMessage(id, "g1", "t1", accountId,
                sender, recipients, null, EmailDirection.INBOUND, now,
                "s", "b", null, List.of(), null, false, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_rejectsNullDirection() {
        assertThatThrownBy(() -> new EmailMessage(id, "g1", "t1", accountId,
                sender, recipients, domain, null, now,
                "s", "b", null, List.of(), null, false, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_rejectsNullSentAt() {
        assertThatThrownBy(() -> new EmailMessage(id, "g1", "t1", accountId,
                sender, recipients, domain, EmailDirection.INBOUND, null,
                "s", "b", null, List.of(), null, false, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_rejectsNullAttachments() {
        assertThatThrownBy(() -> new EmailMessage(id, "g1", "t1", accountId,
                sender, recipients, domain, EmailDirection.INBOUND, now,
                "s", "b", null, null, null, false, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_makesDefensiveCopyOfRecipients() {
        List<EmailAddress> mutable = new java.util.ArrayList<>(recipients);
        EmailMessage msg = new EmailMessage(id, "g1", "t1", accountId,
                sender, mutable, domain, EmailDirection.INBOUND, now,
                "s", "b", null, List.of(), null, false, null, null);
        mutable.add(new EmailAddress("extra@x.com"));
        assertThat(msg.getRecipients()).hasSize(1);
    }

    @Test
    void constructor_makesDefensiveCopyOfAttachments() {
        List<EmailAttachmentMetadata> mutable = new java.util.ArrayList<>();
        mutable.add(new EmailAttachmentMetadata("f", "t", 1L, null));
        EmailMessage msg = new EmailMessage(id, "g1", "t1", accountId,
                sender, recipients, domain, EmailDirection.INBOUND, now,
                "s", "b", null, mutable, null, false, null, null);
        mutable.add(new EmailAttachmentMetadata("f2", "t", 1L, null));
        assertThat(msg.getAttachments()).hasSize(1);
    }
}
