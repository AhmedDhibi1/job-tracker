package com.jobtracker.emailmanagement.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "email_messages", indexes = {
    @Index(name = "idx_email_account_sent", columnList = "email_account_id, sent_at"),
    @Index(name = "idx_gmail_thread_id", columnList = "gmail_thread_id"),
    @Index(name = "idx_application_thread", columnList = "application_thread_id"),
    @Index(name = "idx_gmail_message_id", columnList = "gmail_message_id")
})
public class EmailMessageJpaEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "gmail_message_id", nullable = false, unique = true, length = 100)
    private String gmailMessageId;

    @Column(name = "gmail_thread_id", nullable = false, length = 100)
    private String gmailThreadId;

    @Column(name = "subject", nullable = false, length = 1000)
    private String subject;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "sender_email", nullable = false, length = 320)
    private String senderEmail;

    @ElementCollection
    @CollectionTable(name = "email_message_recipients", joinColumns = @JoinColumn(name = "email_message_id"))
    @Column(name = "recipient_email", length = 320)
    private List<String> recipients = new ArrayList<>();

    @Column(name = "company_domain", nullable = false, length = 253)
    private String companyDomain;

    @Column(name = "direction", nullable = false, length = 32)
    private String direction;

    @Column(name = "classification", length = 30)
    private String classification;

    @Column(name = "classification_result", columnDefinition = "TEXT")
    private String classificationResult;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private Double classificationScore;

    @Column(name = "classification_confidence", length = 10)
    private String classificationConfidence;

    @Column(name = "processed", nullable = false)
    private boolean processed;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "email_message_id")
    private List<EmailAttachmentMetadataJpaEntity> attachments = new ArrayList<>();

    @Column(name = "email_account_id", nullable = false)
    private UUID emailAccountId;

    @Column(name = "application_thread_id")
    private UUID applicationThreadId;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getGmailMessageId() { return gmailMessageId; }
    public void setGmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; }
    public String getGmailThreadId() { return gmailThreadId; }
    public void setGmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }
    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }
    public String getCompanyDomain() { return companyDomain; }
    public void setCompanyDomain(String companyDomain) { this.companyDomain = companyDomain; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    public String getClassificationResult() { return classificationResult; }
    public void setClassificationResult(String classificationResult) { this.classificationResult = classificationResult; }
    public Double getClassificationScore() { return classificationScore; }
    public void setClassificationScore(Double classificationScore) { this.classificationScore = classificationScore; }
    public String getClassificationConfidence() { return classificationConfidence; }
    public void setClassificationConfidence(String classificationConfidence) { this.classificationConfidence = classificationConfidence; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public List<EmailAttachmentMetadataJpaEntity> getAttachments() { return attachments; }
    public void setAttachments(List<EmailAttachmentMetadataJpaEntity> attachments) { this.attachments = attachments; }
    public UUID getEmailAccountId() { return emailAccountId; }
    public void setEmailAccountId(UUID emailAccountId) { this.emailAccountId = emailAccountId; }
    public UUID getApplicationThreadId() { return applicationThreadId; }
    public void setApplicationThreadId(UUID applicationThreadId) { this.applicationThreadId = applicationThreadId; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
