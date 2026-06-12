package com.jobtracker.emailmanagement.infrastructure.gmail.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryDeltaInfraResultTest {

    @Test
    void preservesValues() {
        GmailHistoryRecord record = new GmailHistoryRecord("h2", List.of("msg1"));
        HistoryDeltaInfraResult result = new HistoryDeltaInfraResult(List.of(record), "latestId");
        assertThat(result.records()).hasSize(1);
        assertThat(result.latestHistoryId()).isEqualTo("latestId");
    }
}
