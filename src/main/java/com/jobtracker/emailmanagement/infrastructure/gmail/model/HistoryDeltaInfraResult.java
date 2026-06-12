package com.jobtracker.emailmanagement.infrastructure.gmail.model;

import java.util.List;

public record HistoryDeltaInfraResult(
        List<GmailHistoryRecord> records,
        String latestHistoryId
) {}
