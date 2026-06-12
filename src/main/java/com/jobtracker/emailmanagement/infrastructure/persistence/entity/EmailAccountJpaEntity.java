package com.jobtracker.emailmanagement.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_accounts", indexes = {
    @Index(name = "idx_email_account_active", columnList = "active")
})
public class EmailAccountJpaEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email_address", nullable = false, unique = true, length = 320)
    private String emailAddress;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "encrypted_access_token", nullable = false, length = 2000)
    private String encryptedAccessToken;

    @Column(name = "encrypted_refresh_token", nullable = false, length = 2000)
    private String encryptedRefreshToken;

    @Column(name = "token_expiry", nullable = false)
    private Instant tokenExpiry;

    @Column(name = "sync_history_id", length = 100)
    private String historyId;

    @Column(name = "watch_expiration")
    private Instant watchExpiration;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "empty_poll_count", nullable = false)
    private int emptyPollCount;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    public String getEncryptedAccessToken() { return encryptedAccessToken; }
    public void setEncryptedAccessToken(String encryptedAccessToken) { this.encryptedAccessToken = encryptedAccessToken; }
    public String getEncryptedRefreshToken() { return encryptedRefreshToken; }
    public void setEncryptedRefreshToken(String encryptedRefreshToken) { this.encryptedRefreshToken = encryptedRefreshToken; }
    public Instant getTokenExpiry() { return tokenExpiry; }
    public void setTokenExpiry(Instant tokenExpiry) { this.tokenExpiry = tokenExpiry; }
    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }
    public Instant getWatchExpiration() { return watchExpiration; }
    public void setWatchExpiration(Instant watchExpiration) { this.watchExpiration = watchExpiration; }
    public boolean isPushEnabled() { return pushEnabled; }
    public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }
    public int getEmptyPollCount() { return emptyPollCount; }
    public void setEmptyPollCount(int emptyPollCount) { this.emptyPollCount = emptyPollCount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
