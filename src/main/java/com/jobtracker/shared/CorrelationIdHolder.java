package com.jobtracker.shared;

import org.slf4j.MDC;
import java.util.UUID;

public final class CorrelationIdHolder {

    private CorrelationIdHolder() {}

    public static String current() {
        String id = MDC.get("correlationId");
        return id != null ? id : UUID.randomUUID().toString();
    }
}
