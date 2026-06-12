package com.jobtracker.emailmanagement.domain.model;

import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class EmailMessage {

    private final UUID         id;
    private final String       gmailMessageId;
    private final String       gmailThreadId;
    private final UUID         emailAccountId;
    private final EmailAddress sender;
    private final List<EmailAddress> recipients;
    private final CompanyDomain      companyDomain;
    private final EmailDirection     direction;
    private final Instant            sentAt;
    private final String             subject;
    private final String             bodyText;
    private final String             bodyHtml;
    private final List<EmailAttachmentMetadata> attachments;

    private ClassificationResult classificationResult;
    private boolean processed;
    private UUID    applicationThreadId;
    private Long    version;


    public EmailMessage(
            UUID id, String gmailMessageId, String gmailThreadId, UUID emailAccountId,
            EmailAddress sender, List<EmailAddress> recipients, CompanyDomain companyDomain,
            EmailDirection direction, Instant sentAt,
            String subject, String bodyText, String bodyHtml,
            List<EmailAttachmentMetadata> attachments,
            ClassificationResult classificationResult,
            boolean processed,
            UUID applicationThreadId, Long version) {
        this.id                   = Objects.requireNonNull(id);
        this.gmailMessageId       = Objects.requireNonNull(gmailMessageId);
        this.gmailThreadId        = Objects.requireNonNull(gmailThreadId);
        this.emailAccountId       = Objects.requireNonNull(emailAccountId);
        this.sender               = Objects.requireNonNull(sender);
        this.recipients           = List.copyOf(Objects.requireNonNull(recipients));
        this.companyDomain        = Objects.requireNonNull(companyDomain);
        this.direction            = Objects.requireNonNull(direction);
        this.sentAt               = Objects.requireNonNull(sentAt);
        this.subject              = subject != null ? subject : "";
        this.bodyText             = bodyText != null ? bodyText : "";
        this.bodyHtml             = bodyHtml != null ? bodyHtml : "";
        this.attachments          = List.copyOf(Objects.requireNonNull(attachments));
        this.classificationResult = classificationResult;
        this.processed            = processed;
        this.applicationThreadId  = applicationThreadId;
        this.version              = version;
    }


    public static EmailMessage create(
            UUID id, String gmailMessageId, String gmailThreadId, UUID emailAccountId,
            EmailAddress sender, List<EmailAddress> recipients, CompanyDomain companyDomain,
            EmailDirection direction, Instant sentAt,
            String subject, String bodyText, String bodyHtml,
            List<EmailAttachmentMetadata> attachments) {
        return new EmailMessage(
                id, gmailMessageId, gmailThreadId, emailAccountId,
                sender, recipients, companyDomain, direction, sentAt,
                subject, bodyText, bodyHtml, attachments,
                null, false, null, null);
    }


    public void markClassified(ClassificationResult result) {
        if (this.processed) {
            throw new IllegalStateException(
                    "EmailMessage " + id + " has already been processed — cannot re-classify.");
        }
        this.classificationResult = Objects.requireNonNull(result);
        this.processed            = true;
    }


    public void linkToThread(UUID threadId) {
        if (this.applicationThreadId != null) {
            throw new IllegalStateException(
                    "Message " + id + " is already linked to thread " + applicationThreadId);
        }
        this.applicationThreadId = Objects.requireNonNull(threadId, "threadId must not be null");
    }


    public UUID         getId()                   { return id; }
    public String       getGmailMessageId()       { return gmailMessageId; }
    public String       getGmailThreadId()        { return gmailThreadId; }
    public UUID         getEmailAccountId()       { return emailAccountId; }
    public EmailAddress getSender()               { return sender; }
    public List<EmailAddress> getRecipients()     { return recipients; }
    public CompanyDomain      getCompanyDomain()  { return companyDomain; }
    public EmailDirection     getDirection()      { return direction; }
    public Instant            getSentAt()         { return sentAt; }
    public String       getSubject()              { return subject; }
    public String       getBodyText()             { return bodyText; }
    public String       getBodyHtml()             { return bodyHtml; }
    public List<EmailAttachmentMetadata> getAttachments() { return attachments; }
    public ClassificationResult getClassificationResultObj() { return classificationResult; }
    public String       getClassification()           { return classificationResult != null ? classificationResult.classification() : null; }
    public String       getClassificationResult()     { return classificationResult != null ? classificationResult.serializedResult() : null; }
    public Double       getClassificationScore()      { return classificationResult != null ? classificationResult.score() : null; }
    public String       getClassificationConfidence() { return classificationResult != null ? classificationResult.confidence() : null; }
    public boolean      isProcessed()                 { return processed; }
    public UUID         getApplicationThreadId()      { return applicationThreadId; }
    public Long         getVersion()                  { return version; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String gmailMessageId;
        private String gmailThreadId;
        private UUID emailAccountId;
        private EmailAddress sender;
        private List<EmailAddress> recipients;
        private CompanyDomain companyDomain;
        private EmailDirection direction;
        private Instant sentAt;
        private String subject;
        private String bodyText;
        private String bodyHtml;
        private List<EmailAttachmentMetadata> attachments;
        private ClassificationResult classificationResult;
        private boolean processed;
        private UUID applicationThreadId;
        private Long version;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder gmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; return this; }
        public Builder gmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; return this; }
        public Builder emailAccountId(UUID emailAccountId) { this.emailAccountId = emailAccountId; return this; }
        public Builder sender(EmailAddress sender) { this.sender = sender; return this; }
        public Builder recipients(List<EmailAddress> recipients) { this.recipients = recipients; return this; }
        public Builder companyDomain(CompanyDomain companyDomain) { this.companyDomain = companyDomain; return this; }
        public Builder direction(EmailDirection direction) { this.direction = direction; return this; }
        public Builder sentAt(Instant sentAt) { this.sentAt = sentAt; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder bodyText(String bodyText) { this.bodyText = bodyText; return this; }
        public Builder bodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; return this; }
        public Builder attachments(List<EmailAttachmentMetadata> attachments) { this.attachments = attachments; return this; }
        public Builder classificationResult(ClassificationResult classificationResult) { this.classificationResult = classificationResult; return this; }
        public Builder processed(boolean processed) { this.processed = processed; return this; }
        public Builder applicationThreadId(UUID applicationThreadId) { this.applicationThreadId = applicationThreadId; return this; }
        public Builder version(Long version) { this.version = version; return this; }

        public EmailMessage build() {
            return new EmailMessage(
                    id, gmailMessageId, gmailThreadId, emailAccountId,
                    sender, recipients, companyDomain, direction, sentAt,
                    subject, bodyText, bodyHtml, attachments,
                    classificationResult, processed, applicationThreadId, version);
        }
    }
}
