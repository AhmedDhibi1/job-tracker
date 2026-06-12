package com.jobtracker.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyDomainTest {

    @Test
    void acceptsValidDomain() {
        CompanyDomain domain = new CompanyDomain("example.com");
        assertThat(domain.value()).isEqualTo("example.com");
    }

    @Test
    void stripsLeadingAtSymbol() {
        CompanyDomain domain = new CompanyDomain("@example.com");
        assertThat(domain.value()).isEqualTo("example.com");
    }

    @Test
    void normalizesToLowercase() {
        CompanyDomain domain = new CompanyDomain("Example.COM");
        assertThat(domain.value()).isEqualTo("example.com");
    }

    @Test
    void stripsWhitespace() {
        CompanyDomain domain = new CompanyDomain("  example.com  ");
        assertThat(domain.value()).isEqualTo("example.com");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new CompanyDomain(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> new CompanyDomain("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    @Test
    void rejectsDomainWithOnlyAt() {
        assertThatThrownBy(() -> new CompanyDomain("@"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void from_createsFromEmailAddress() {
        EmailAddress email = new EmailAddress("user@acme.com");
        CompanyDomain domain = CompanyDomain.from(email);
        assertThat(domain.value()).isEqualTo("acme.com");
    }

    @Test
    void from_rejectsNullEmail() {
        assertThatThrownBy(() -> CompanyDomain.from(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equality() {
        CompanyDomain d1 = new CompanyDomain("example.com");
        CompanyDomain d2 = new CompanyDomain("Example.COM");
        assertThat(d1).isEqualTo(d2);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }

    @Test
    void inequality() {
        CompanyDomain d1 = new CompanyDomain("a.com");
        CompanyDomain d2 = new CompanyDomain("b.com");
        assertThat(d1).isNotEqualTo(d2);
    }

    @Test
    void toString_returnsValue() {
        CompanyDomain domain = new CompanyDomain("example.com");
        assertThat(domain.toString()).isEqualTo("example.com");
    }
}
