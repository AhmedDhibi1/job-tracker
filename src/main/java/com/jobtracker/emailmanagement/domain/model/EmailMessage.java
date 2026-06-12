package com.jobtracker.emailmanagement.domain.model;

import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import static com.jobtracker.emailmanagement.domain.model.EmailDirection.*;
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
    private final List<EmailAddress> recipients; // unmodifiable
    private final CompanyDomain      companyDomain;
    private final EmailDirection     direction;
    private final Instant            sentAt;
    private final String             subject;
    private final String             bodyText;
    private final String             bodyHtml;
    private final List<EmailAttachmentMetadata> attachments; // unmodifiable

    private       String  classification;        
    private       String  classificationResult;  
    private       Double  classificationScore;
    private       String  classificationConfidence;
    private       boolean processed;
    private       UUID    applicationThreadId;   
    private       Long    version;


    public EmailMessage(
            UUID id, String gmailMessageId, String gmailThreadId, UUID emailAccountId,
            EmailAddress sender, List<EmailAddress> recipients, CompanyDomain companyDomain,
            EmailDirection direction, Instant sentAt,
            String subject, String bodyText, String bodyHtml,
            List<EmailAttachmentMetadata> attachments,
            String classification, String classificationResult,
            Double classificationScore, String classificationConfidence,
            boolean processed,
            UUID applicationThreadId, Long version) {
        this.id                   = Objects.requireNonNull(id);
        this.gmailMessageId       = Objects.requireNonNull(gmailMessageId);
        this.gmailThreadId        = Objects.requireNonNull(gmailThreadId);
        this.emailAccountId       = Objects.requireNonNull(emailAccountId);
        this.sender               = Objects.requireNonNull(sender);
        this.recipients           = List.copyOf(
                                        Objects.requireNonNull(recipients));
        this.companyDomain        = Objects.requireNonNull(companyDomain);
        this.direction            = Objects.requireNonNull(direction);
        this.sentAt               = Objects.requireNonNull(sentAt);
        this.subject              = subject != null ? subject : "";
        this.bodyText             = bodyText != null ? bodyText : "";
        this.bodyHtml             = bodyHtml != null ? bodyHtml : "";
        this.attachments          = List.copyOf(
                                        Objects.requireNonNull(attachments));
        this.classification       = classification;
        this.classificationResult = classificationResult;
        this.classificationScore  = classificationScore;
        this.classificationConfidence = classificationConfidence;
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
                null, null, null, null, false, null, null);
    }


    public void markClassified(String classificationEnumName, String serializedResult,
                               Double score, String confidence) {
        if (this.processed) {
            throw new IllegalStateException(
                    "EmailMessage " + id + " has already been processed — cannot re-classify.");
        }
        this.classification           = Objects.requireNonNull(classificationEnumName);
        this.classificationResult     = serializedResult;
        this.classificationScore      = score;
        this.classificationConfidence = confidence;
        this.processed                = true;
    }


    public void linkToThread(UUID threadId) {
        if (this.processed) {
            throw new IllegalStateException(
                    "Cannot link thread after processing: " + id);
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
    public String       getClassification()           { return classification; }
    public String       getClassificationResult()     { return classificationResult; }
    public Double       getClassificationScore()      { return classificationScore; }
    public String       getClassificationConfidence() { return classificationConfidence; }
    public boolean      isProcessed()                 { return processed; }
    public UUID         getApplicationThreadId()      { return applicationThreadId; }
    public Long         getVersion()                  { return version; }
}