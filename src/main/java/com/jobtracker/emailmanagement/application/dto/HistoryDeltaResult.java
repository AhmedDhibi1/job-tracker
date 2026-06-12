package com.jobtracker.emailmanagement.application.dto;

import java.util.List;

public record HistoryDeltaResult(
        List<EmailHistoryDelta> records,
        String latestHistoryId
) {}
