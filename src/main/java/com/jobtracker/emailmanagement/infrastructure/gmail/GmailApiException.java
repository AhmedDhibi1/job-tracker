package com.jobtracker.emailmanagement.infrastructure.gmail;

public class GmailApiException extends RuntimeException {

    private final String accountId;
    private final Operation operation;

    public GmailApiException(String message, Throwable cause, String accountId, Operation operation) {
        super(message, cause);
        this.accountId = accountId;
        this.operation = operation;
    }

    public String getAccountId() { return accountId; }
    public Operation getOperation() { return operation; }

    public enum Operation {
        FETCH_MESSAGE,
        LIST_MESSAGES,
        FETCH_HISTORY,
        SETUP_WATCH,
        STOP_WATCH
    }
}
