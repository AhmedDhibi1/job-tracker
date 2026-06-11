package com.jobtracker.emailmanagement.infrastructure.gmail.model;

public record GmailHistoryRecord(
        String newHistoryId,
        java.util.List<String> addedMessageIds
) {}