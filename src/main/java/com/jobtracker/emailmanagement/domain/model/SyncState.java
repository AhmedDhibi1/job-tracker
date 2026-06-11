package com.jobtracker.emailmanagement.domain.model;

import java.time.Instant;
import java.util.Objects;


public record SyncState(
        String historyId,
        Instant watchExpiration,
        boolean pushEnabled
) {

    public static SyncState initial() {
        return new SyncState(null, null, false);
    }

    public boolean isWatchExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return watchExpiration != null && !now.isBefore(watchExpiration);
    }


    public SyncState withUpdatedHistoryId(String newHistoryId) {
        return new SyncState(newHistoryId, this.watchExpiration, this.pushEnabled);
    }


    public SyncState withWatchRegistered(Instant expiration) {
        Objects.requireNonNull(expiration, "expiration must not be null");
        return new SyncState(this.historyId, expiration, true);
    }
}