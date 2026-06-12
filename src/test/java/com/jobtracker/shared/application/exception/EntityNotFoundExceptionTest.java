package com.jobtracker.shared.application.exception;

import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EntityNotFoundExceptionTest {

    @Test
    void constructor_setsCorrectCodeAndMessage() {
        UUID id = UUID.randomUUID();
        EntityNotFoundException e = new EntityNotFoundException(EmailAccount.class, id);
        assertThat(e.getErrorCode()).isEqualTo("ENTITY_NOT_FOUND");
        assertThat(e.getMessage()).contains("EmailAccount");
        assertThat(e.getMessage()).contains(id.toString());
        assertThat(e.getEntityType()).isEqualTo(EmailAccount.class);
        assertThat(e.getEntityId()).isEqualTo(id);
    }
}
