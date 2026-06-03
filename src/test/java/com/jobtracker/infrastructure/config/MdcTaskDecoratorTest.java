package com.jobtracker.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class MdcTaskDecoratorTest {
    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void decorateCopiesMdcContext() {
        MDC.put("correlationId", "corr-123");
        MdcTaskDecorator decorator = new MdcTaskDecorator();
        AtomicReference<String> captured = new AtomicReference<>();

        Runnable decorated = decorator.decorate(() -> captured.set(MDC.get("correlationId")));
        MDC.clear();

        decorated.run();

        assertThat(captured.get()).isEqualTo("corr-123");
    }
}
