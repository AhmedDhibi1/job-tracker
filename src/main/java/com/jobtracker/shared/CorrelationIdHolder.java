package com.jobtracker.shared;

import org.slf4j.MDC;
import java.util.UUID;

public final class CorrelationIdHolder {

    private CorrelationIdHolder() {}

    private static final String MDC_KEY = "correlationId";

    public static String current() {
        String id = MDC.get(MDC_KEY);
        return id != null ? id : UUID.randomUUID().toString();
    }

    public static String generateNew() {
        String id = UUID.randomUUID().toString();
        MDC.put(MDC_KEY, id);
        return id;
    }

    public static void set(String correlationId) {
        MDC.put(MDC_KEY, correlationId);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
