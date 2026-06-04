package com.jobtracker.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link CompanyDomain}.
 */
class CompanyDomainTest {

    // ── Valid construction from string ────────────────────────────────────────

    @Test
    void constructor_acceptsPlainDomain() {
        var domain = new CompanyDomain("google.com");
        assertThat(domain.value()).isEqualTo("google.com");
    }

    @Test
    void constructor_normalizesToLowercase() {
        var domain = new CompanyDomain("Google.COM");
        assertThat(domain.value()).isEqualTo("google.com");
    }

    @Test
    void constructor_stripsLeadingAtSymbol() {
        var domain = new CompanyDomain("@google.com");
        assertThat(domain.value()).isEqualTo("google.com");
    }

    @Test
    void constructor_stripsWhitespace() {
        var domain = new CompanyDomain("  google.com  ");
        assertThat(domain.value()).isEqualTo("google.com");
    }

    @Test
    void constructor_acceptsSubdomain() {
        var domain = new CompanyDomain("careers.amazon.co.uk");
        assertThat(domain.value()).isEqualTo("careers.amazon.co.uk");
    }

    // ── Invalid construction ──────────────────────────────────────────────────

    @Test
    void constructor_rejectsNull() {
        assertThatThrownBy(() -> new CompanyDomain(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    @Test
    void constructor_rejectsBlank() {
        assertThatThrownBy(() -> new CompanyDomain("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    @Test
    void constructor_rejectsAtSymbolOnly() {
        assertThatThrownBy(() -> new CompanyDomain("@"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank after stripping");
    }

    // ── Factory: from(EmailAddress) ───────────────────────────────────────────

    @Test
    void from_extractsDomainFromEmailAddress() {
        var email  = new EmailAddress("recruiter@google.com");
        var domain = CompanyDomain.from(email);
        assertThat(domain.value()).isEqualTo("google.com");
    }

    @Test
    void from_normalizesExtractedDomain() {
        var email  = new EmailAddress("HR@CAREERS.COMPANY.IO");
        var domain = CompanyDomain.from(email);
        assertThat(domain.value()).isEqualTo("careers.company.io");
    }

    @Test
    void from_rejectsNullEmailAddress() {
        assertThatThrownBy(() -> CompanyDomain.from(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("EmailAddress must not be null");
    }

    // ── Equality ─────────────────────────────────────────────────────────────

    @Test
    void equality_isCaseInsensitive() {
        var a = new CompanyDomain("Google.com");
        var b = new CompanyDomain("GOOGLE.COM");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void equality_withAtPrefixVariant() {
        var a = new CompanyDomain("google.com");
        var b = new CompanyDomain("@google.com");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void hashCode_isConsistentWithEquality() {
        var a = new CompanyDomain("google.com");
        var b = new CompanyDomain("GOOGLE.COM");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    void toString_returnsNormalizedValue() {
        var domain = new CompanyDomain("@GOOGLE.COM");
        assertThat(domain.toString()).isEqualTo("google.com");
    }
}