package com.jobtracker.emailmanagement.domain.service;

import com.jobtracker.emailmanagement.domain.model.EmailDirection;
import com.jobtracker.emailmanagement.domain.model.EmailMessage;
import com.jobtracker.emailmanagement.domain.model.RawEmailInput;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailParserDomainServiceTest {

    private final EmailParserDomainService parser = new EmailParserDomainService();

    @Test
    void parse_inboundEmail_setsDirectionInboundAndDerivesCompanyDomain() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput raw = new RawEmailInput(
                "msg123", "thread456", Map.of("from", "recruiter@acme.com"),
                List.of(), Instant.now(),
                new EmailAddress("recruiter@acme.com"),
                List.of(new EmailAddress("user@gmail.com")),
                "Interview", "Hi there", null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getDirection()).isEqualTo(EmailDirection.INBOUND);
        assertThat(result.getCompanyDomain().value()).isEqualTo("acme.com");
    }

    @Test
    void parse_outboundEmail_setsDirectionOutbound() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput raw = new RawEmailInput(
                "msg456", "thread789", Map.of(),
                List.of(), Instant.now(),
                accountAddress,
                List.of(new EmailAddress("recruiter@acme.com")),
                "Re: Interview", "Thanks", null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getDirection()).isEqualTo(EmailDirection.OUTBOUND);
        assertThat(result.getCompanyDomain().value()).isEqualTo("acme.com");
    }

    @Test
    void parse_outboundEmail_derivesDomainFromFirstExternalRecipient() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput raw = new RawEmailInput(
                "msg456", "thread789", Map.of(),
                List.of(), Instant.now(),
                accountAddress,
                List.of(new EmailAddress("user@gmail.com"), new EmailAddress("external@acme.com")),
                "Re: Interview", "Thanks", null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getDirection()).isEqualTo(EmailDirection.OUTBOUND);
        assertThat(result.getCompanyDomain().value()).isEqualTo("acme.com");
    }

    @Test
    void parse_outboundEmail_noExternalRecipient_fallsBackToSenderDomain() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput raw = new RawEmailInput(
                "msg456", "thread789", Map.of(),
                List.of(), Instant.now(),
                accountAddress,
                List.of(new EmailAddress("user@gmail.com")),
                "Note to self", "reminder", null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getDirection()).isEqualTo(EmailDirection.OUTBOUND);
        assertThat(result.getCompanyDomain().value()).isEqualTo("gmail.com");
    }

    @Test
    void parse_extractsBodyText_whenBodyTextProvided() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput raw = new RawEmailInput(
                "msg789", "thread012", Map.of("from", "x@y.com"),
                List.of(), Instant.now(),
                new EmailAddress("x@y.com"),
                List.of(accountAddress),
                "Subject", "Hello World", "<p>Hello World</p>"
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getBodyText()).isEqualTo("Hello World");
        assertThat(result.getBodyHtml()).isEqualTo("<p>Hello World</p>");
    }

    @Test
    void parse_fallsBackToPartBody_whenBodyTextNull() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput.PartInput textPart = new RawEmailInput.PartInput(
                "text/plain", null, "", "Part body", null, 0);
        RawEmailInput raw = new RawEmailInput(
                "msg101", "thread202", Map.of("from", "x@y.com"),
                List.of(textPart), Instant.now(),
                new EmailAddress("x@y.com"),
                List.of(accountAddress),
                "Subject", null, null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getBodyText()).isEqualTo("Part body");
    }

    @Test
    void parse_fallsBackToPartBodyHtml_whenBodyHtmlNull() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput.PartInput htmlPart = new RawEmailInput.PartInput(
                "text/html", null, "", "<p>HTML body</p>", null, 0);
        RawEmailInput raw = new RawEmailInput(
                "msg102", "thread203", Map.of("from", "x@y.com"),
                List.of(htmlPart), Instant.now(),
                new EmailAddress("x@y.com"),
                List.of(accountAddress),
                "Subject", null, null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getBodyHtml()).isEqualTo("<p>HTML body</p>");
    }

    @Test
    void parse_returnsEmptyBodyText_whenNeitherBodyNorPartProvided() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput raw = new RawEmailInput(
                "msg103", "thread204", Map.of("from", "x@y.com"),
                List.of(), Instant.now(),
                new EmailAddress("x@y.com"),
                List.of(accountAddress),
                "Subject", null, null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getBodyText()).isEmpty();
        assertThat(result.getBodyHtml()).isEmpty();
    }

    @Test
    void parse_extractsAttachmentsFromParts() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput.PartInput attachment = new RawEmailInput.PartInput(
                "application/pdf", "attachment; filename=resume.pdf", "resume.pdf", null, "att123", 1024);
        RawEmailInput.PartInput textPart = new RawEmailInput.PartInput(
                "text/plain", null, "", "body", null, 0);
        RawEmailInput raw = new RawEmailInput(
                "msg104", "thread205", Map.of("from", "x@y.com"),
                List.of(attachment, textPart), Instant.now(),
                new EmailAddress("x@y.com"),
                List.of(accountAddress),
                "Subject", "body", null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getAttachments()).hasSize(1);
        assertThat(result.getAttachments().get(0).filename()).isEqualTo("resume.pdf");
        assertThat(result.getAttachments().get(0).gmailAttachmentId()).isEqualTo("att123");
    }

    @Test
    void parse_returnsEmptyAttachments_whenNoAttachmentParts() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput.PartInput textPart = new RawEmailInput.PartInput(
                "text/plain", null, "", "body", null, 0);
        RawEmailInput raw = new RawEmailInput(
                "msg105", "thread206", Map.of("from", "x@y.com"),
                List.of(textPart), Instant.now(),
                new EmailAddress("x@y.com"),
                List.of(accountAddress),
                "Subject", null, null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getAttachments()).isEmpty();
    }

    @Test
    void parse_inboundEmail_sameAccountDomain_stillInfersInboundDomain() {
        EmailAddress accountAddress = new EmailAddress("user@gmail.com");
        RawEmailInput raw = new RawEmailInput(
                "msg106", "thread207", Map.of("from", "other@gmail.com"),
                List.of(), Instant.now(),
                new EmailAddress("other@gmail.com"),
                List.of(new EmailAddress("user@gmail.com")),
                "Subject", "body", null
        );
        EmailMessage result = parser.parse(raw, UUID.randomUUID(), accountAddress);
        assertThat(result.getDirection()).isEqualTo(EmailDirection.INBOUND);
        assertThat(result.getCompanyDomain().value()).isEqualTo("gmail.com");
    }
}
