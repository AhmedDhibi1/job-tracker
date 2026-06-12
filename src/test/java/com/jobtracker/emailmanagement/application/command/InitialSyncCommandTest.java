package com.jobtracker.emailmanagement.application.command;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InitialSyncCommandTest {

    private final UUID accountId = UUID.randomUUID();

    @Test
    void rejectsNullEmailAccountId() {
        assertThatThrownBy(() -> new InitialSyncCommand(null, 30))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("emailAccountId");
    }

    @Test
    void rejectsNegativeDaysBack() {
        assertThatThrownBy(() -> new InitialSyncCommand(accountId, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("daysBack");
    }

    @Test
    void acceptsZeroDaysBack() {
        InitialSyncCommand cmd = new InitialSyncCommand(accountId, 0);
        assertThat(cmd.daysBack()).isZero();
    }

    @Test
    void preservesValues() {
        InitialSyncCommand cmd = new InitialSyncCommand(accountId, 30);
        assertThat(cmd.emailAccountId()).isEqualTo(accountId);
        assertThat(cmd.daysBack()).isEqualTo(30);
    }
}
