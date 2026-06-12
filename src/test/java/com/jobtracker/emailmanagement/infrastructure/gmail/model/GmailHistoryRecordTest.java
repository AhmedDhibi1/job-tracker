package com.jobtracker.emailmanagement.infrastructure.gmail.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GmailHistoryRecordTest {

    @Test
    void preservesValues() {
        GmailHistoryRecord record = new GmailHistoryRecord("h2", List.of("msg1", "msg2"));
        assertThat(record.newHistoryId()).isEqualTo("h2");
        assertThat(record.addedMessageIds()).containsExactly("msg1", "msg2");
    }
}
