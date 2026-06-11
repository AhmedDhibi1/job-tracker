package com.jobtracker.emailmanagement.domain.model;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import java.util.Objects;
import java.util.UUID;


public class EmailAccount {

    private final UUID id;
    private final EmailAddress emailAddress;
    private String displayName;
    private boolean isPrimary;
    private OAuthTokenPair oauthTokens;
    private SyncState syncState;
    private int emptyPollCount;
    private boolean active;
    private Long version;   


    public EmailAccount(
            UUID id,
            EmailAddress emailAddress,
            String displayName,
            boolean isPrimary,
            OAuthTokenPair oauthTokens,
            SyncState syncState,
            int emptyPollCount,
            boolean active,
            Long version) {
        this.id = Objects.requireNonNull(id,"id must not be null");
        this.emailAddress = Objects.requireNonNull(emailAddress,"emailAddress must not be null");
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
        this.isPrimary = isPrimary;
        this.oauthTokens = Objects.requireNonNull(oauthTokens,"oauthTokens must not be null");
        this.syncState = Objects.requireNonNull(syncState, "syncState must not be null");
        this.emptyPollCount = emptyPollCount;
        this.active = active;
        this.version = version;
    }


    public static EmailAccount create(
            UUID id,
            EmailAddress emailAddress,
            String displayName,
            boolean isPrimary,
            OAuthTokenPair oauthTokens) {
        return new EmailAccount(
                id, emailAddress, displayName, isPrimary,
                oauthTokens, SyncState.initial(), 0, true, 0L);
    }


    public static EmailAccount reconstitute(UUID id, EmailAddress email, String displayName,
                                            boolean isPrimary, OAuthTokenPair tokens, SyncState syncState,
                                            int emptyPollCount, boolean active, Long version) {
        return new EmailAccount(id, email, displayName, isPrimary, tokens,
                                syncState, emptyPollCount, active, version);
    }


    public void updateTokens(OAuthTokenPair newTokens) {
        if (!active) {
            throw new IllegalStateException(
                    "Cannot update tokens on a deactivated account: " + id);
        }
        this.oauthTokens = Objects.requireNonNull(newTokens, "newTokens must not be null");
    }

    /** Updates the Gmail synchronization state. */
    public void updateSyncState(SyncState newState) {
        this.syncState = Objects.requireNonNull(newState, "newState must not be null");
    }

    public void incrementEmptyPollCount() {
        this.emptyPollCount++;
    }

    public void resetEmptyPollCount() {
        this.emptyPollCount = 0;
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Account " + id + " is already deactivated");
        }
        this.active = false;
    }

    public void reactivate() {
        if (this.active) {
            throw new IllegalStateException("Account " + id + " is already active");
        }
        this.active = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAccount other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public UUID         getId()             { return id; }
    public EmailAddress getEmailAddress()   { return emailAddress; }
    public String       getDisplayName()    { return displayName; }
    public boolean      isPrimary()         { return isPrimary; }
    public OAuthTokenPair getOauthTokens()  { return oauthTokens; }
    public SyncState    getSyncState()      { return syncState; }
    public int          getEmptyPollCount() { return emptyPollCount; }
    public boolean      isActive()          { return active; }
    public Long         getVersion()        { return version; }
}