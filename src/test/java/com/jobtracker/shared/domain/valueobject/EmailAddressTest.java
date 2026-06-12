package com.jobtracker.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAddressTest {

    @Test
    void acceptsValidEmail() {
        EmailAddress email = new EmailAddress("test@example.com");
        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new EmailAddress(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> new EmailAddress("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    @Test
    void rejectsInvalidFormat() {
        assertThatThrownBy(() -> new EmailAddress("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email");
    }

    @Test
    void stripsWhitespace() {
        EmailAddress email = new EmailAddress("  test@example.com  ");
        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @Test
    void convertsToLowercase() {
        EmailAddress email = new EmailAddress("Test@Example.COM");
        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @Test
    void domain_returnsCorrectDomain() {
        EmailAddress email = new EmailAddress("user@example.com");
        assertThat(email.domain()).isEqualTo("example.com");
    }

    @Test
    void domain_handlesSubdomain() {
        EmailAddress email = new EmailAddress("user@mail.example.co.uk");
        assertThat(email.domain()).isEqualTo("mail.example.co.uk");
    }

    @Test
    void toString_returnsValue() {
        EmailAddress email = new EmailAddress("test@example.com");
        assertThat(email.toString()).isEqualTo("test@example.com");
    }

    @Test
    void equality() {
        EmailAddress e1 = new EmailAddress("test@example.com");
        EmailAddress e2 = new EmailAddress("Test@EXAMPLE.com");
        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }

    @Test
    void inequality() {
        EmailAddress e1 = new EmailAddress("a@x.com");
        EmailAddress e2 = new EmailAddress("b@x.com");
        assertThat(e1).isNotEqualTo(e2);
    }
}
