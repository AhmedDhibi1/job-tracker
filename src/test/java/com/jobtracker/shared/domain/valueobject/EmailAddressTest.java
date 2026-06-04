package com.jobtracker.shared.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link EmailAddress}.
 */
class EmailAddressTest {

    // ── Valid construction ────────────────────────────────────────────────────

    @Test
    void constructor_acceptsTypicalEmailAddress() {
        var email = new EmailAddress("recruiter@google.com");
        assertThat(email.value()).isEqualTo("recruiter@google.com");
    }

    @Test
    void constructor_normalizesToLowercase() {
        var email = new EmailAddress("Recruiter@Google.COM");
        assertThat(email.value()).isEqualTo("recruiter@google.com");
    }

    @Test
    void constructor_stripsLeadingAndTrailingWhitespace() {
        var email = new EmailAddress("  user@example.com  ");
        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    void constructor_acceptsSubdomainEmail() {
        var email = new EmailAddress("hr@careers.amazon.co.uk");
        assertThat(email.value()).isEqualTo("hr@careers.amazon.co.uk");
    }

    @Test
    void constructor_acceptsPlusAddressedEmail() {
        var email = new EmailAddress("user+jobs@gmail.com");
        assertThat(email.value()).isEqualTo("user+jobs@gmail.com");
    }

    // ── Invalid construction ─────────────────────────────────────────────────

    @Test
    void constructor_rejectsNullValue() {
        assertThatThrownBy(() -> new EmailAddress(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    @Test
    void constructor_rejectsBlankValue() {
        assertThatThrownBy(() -> new EmailAddress("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    @ParameterizedTest(name = "rejects invalid format: [{0}]")
    @ValueSource(strings = {
            "not-an-email",           // no @ symbol
            "@nodomain",              // no local part
            "noDomain@",              // no domain
            "user@",                  // empty domain
            "user@domain",            // no TLD
            "user @domain.com",       // space in local part
            "user@domain..com",       // double dot in domain
            "plaintext",              // no @ at all
    })
    void constructor_rejectsInvalidFormats(String invalid) {
        assertThatThrownBy(() -> new EmailAddress(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email address format");
    }

    // ── domain() ─────────────────────────────────────────────────────────────

    @Test
    void domain_extractsDomainPart() {
        var email = new EmailAddress("user@google.com");
        assertThat(email.domain()).isEqualTo("google.com");
    }

    @Test
    void domain_extractsSubdomain() {
        var email = new EmailAddress("hr@careers.company.io");
        assertThat(email.domain()).isEqualTo("careers.company.io");
    }

    // ── Equality (case-insensitive) ───────────────────────────────────────────

    @Test
    void equality_isCaseInsensitive() {
        var lower  = new EmailAddress("user@example.com");
        var upper  = new EmailAddress("USER@EXAMPLE.COM");
        var mixed  = new EmailAddress("User@Example.Com");
        assertThat(lower).isEqualTo(upper);
        assertThat(lower).isEqualTo(mixed);
    }

    @Test
    void hashCode_isConsistentWithEquality() {
        var a = new EmailAddress("user@example.com");
        var b = new EmailAddress("USER@EXAMPLE.COM");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    void toString_returnsNormalizedValue() {
        var email = new EmailAddress("USER@EXAMPLE.COM");
        assertThat(email.toString()).isEqualTo("user@example.com");
    }
}