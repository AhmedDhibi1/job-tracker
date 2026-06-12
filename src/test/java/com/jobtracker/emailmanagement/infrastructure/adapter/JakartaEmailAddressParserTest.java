package com.jobtracker.emailmanagement.infrastructure.adapter;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JakartaEmailAddressParserTest {

    private final JakartaEmailAddressParser parser = new JakartaEmailAddressParser();

    @Test
    void parse_returnsEmptyListForNullInput() {
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    void parse_returnsEmptyListForBlankInput() {
        assertThat(parser.parse("   ")).isEmpty();
    }

    @Test
    void parse_singleAddress() {
        List<EmailAddress> result = parser.parse("user@example.com");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).value()).isEqualTo("user@example.com");
    }

    @Test
    void parse_multipleAddresses() {
        List<EmailAddress> result = parser.parse("user1@example.com, user2@test.com");
        assertThat(result).hasSize(2);
    }

    @Test
    void parse_withDisplayName() {
        List<EmailAddress> result = parser.parse("\"John Doe\" <john@example.com>");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).value()).isEqualTo("john@example.com");
    }

    @Test
    void parse_throwsOnInvalidEmailAddress() {
        assertThatThrownBy(() -> parser.parse("invalid-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parse_triggersFallback_whenInternetAddressFails() {
        assertThatThrownBy(() -> parser.parse("a b@c.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseSingle_parsesEmailFromHeader() {
        EmailAddress result = parser.parseSingle("user@example.com");
        assertThat(result.value()).isEqualTo("user@example.com");
    }

    @Test
    void parseSingle_withDisplayName() {
        EmailAddress result = parser.parseSingle("\"John\" <john@example.com>");
        assertThat(result.value()).isEqualTo("john@example.com");
    }

    @Test
    void parseSingle_throwsOnNullInput() {
        assertThatThrownBy(() -> parser.parseSingle(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseSingle_throwsOnBlankInput() {
        assertThatThrownBy(() -> parser.parseSingle("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseSingle_throwsOnInvalidInput() {
        assertThatThrownBy(() -> parser.parseSingle("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseSingle_throwsWithMessage_whenInternetAddressFails() {
        assertThatThrownBy(() -> parser.parseSingle("a b@c.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot parse email address");
    }
}
