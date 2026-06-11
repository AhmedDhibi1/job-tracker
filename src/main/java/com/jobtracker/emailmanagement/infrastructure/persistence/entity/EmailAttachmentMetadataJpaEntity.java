package com.jobtracker.emailmanagement.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "email_attachments")
public class EmailAttachmentMetadataJpaEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_message_id", nullable = false)
    private EmailMessageJpaEntity emailMessage;

    @Column(name = "filename", nullable = false, length = 500)
    private String filename;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "gmail_attachment_id", nullable = false, length = 100)
    private String gmailAttachmentId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public EmailMessageJpaEntity getEmailMessage() { return emailMessage; }
    public void setEmailMessage(EmailMessageJpaEntity emailMessage) { this.emailMessage = emailMessage; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getGmailAttachmentId() { return gmailAttachmentId; }
    public void setGmailAttachmentId(String gmailAttachmentId) { this.gmailAttachmentId = gmailAttachmentId; }
}
