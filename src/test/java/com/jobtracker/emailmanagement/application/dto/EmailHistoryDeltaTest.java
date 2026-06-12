package com.jobtracker.emailmanagement.application.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmailHistoryDeltaTest {

    @Test
    void preservesValues() {
        EmailHistoryDelta delta = new EmailHistoryDelta("h2", List.of("msg1", "msg2"));
        assertThat(delta.newHistoryId()).isEqualTo("h2");
        assertThat(delta.addedMessageIds()).containsExactly("msg1", "msg2");
    }

    @Test
    void acceptsEmptyMessageIds() {
        EmailHistoryDelta delta = new EmailHistoryDelta("h1", List.of());
        assertThat(delta.addedMessageIds()).isEmpty();
    }
}
