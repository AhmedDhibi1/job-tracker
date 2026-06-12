package com.jobtracker.emailmanagement.application.dto;

import java.util.List;

public record EmailHistoryDelta(
        String newHistoryId,
        List<String> addedMessageIds
) {}
