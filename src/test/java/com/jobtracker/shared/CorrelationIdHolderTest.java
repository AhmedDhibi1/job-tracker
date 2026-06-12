package com.jobtracker.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdHolderTest {

    @AfterEach
    void tearDown() {
        CorrelationIdHolder.clear();
    }

    @Test
    void current_returnsRandomUuid_whenNotSet() {
        String id = CorrelationIdHolder.current();
        assertThat(id).isNotNull();
    }

    @Test
    void setAndCurrent_returnsSetValue() {
        CorrelationIdHolder.set("my-corr-id");
        assertThat(CorrelationIdHolder.current()).isEqualTo("my-corr-id");
    }

    @Test
    void generateNew_createsNewValueAndSetsIt() {
        String id = CorrelationIdHolder.generateNew();
        assertThat(id).isNotNull();
        assertThat(MDC.get("correlationId")).isEqualTo(id);
    }

    @Test
    void clear_removesFromMDC() {
        CorrelationIdHolder.set("test");
        CorrelationIdHolder.clear();
        assertThat(MDC.get("correlationId")).isNull();
    }
}
