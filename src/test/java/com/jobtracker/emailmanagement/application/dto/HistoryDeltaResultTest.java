package com.jobtracker.emailmanagement.application.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryDeltaResultTest {

    @Test
    void preservesValues() {
        EmailHistoryDelta delta = new EmailHistoryDelta("h2", List.of("msg1", "msg2"));
        HistoryDeltaResult result = new HistoryDeltaResult(List.of(delta), "latestH1");
        assertThat(result.records()).containsExactly(delta);
        assertThat(result.latestHistoryId()).isEqualTo("latestH1");
    }

    @Test
    void acceptsEmptyRecords() {
        HistoryDeltaResult result = new HistoryDeltaResult(List.of(), "h1");
        assertThat(result.records()).isEmpty();
    }
}
