package com.jobtracker.emailmanagement.domain.model;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawEmailInputTest {

    private final EmailAddress from = new EmailAddress("sender@acme.com");

    @Test
    void rejectsNullGmailMessageId() {
        assertThatThrownBy(() -> new RawEmailInput(null, "thread1", Map.of(), List.of(), Instant.now(), from, List.of(), "subj", null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullGmailThreadId() {
        assertThatThrownBy(() -> new RawEmailInput("msg1", null, Map.of(), List.of(), Instant.now(), from, List.of(), "subj", null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullSentAt() {
        assertThatThrownBy(() -> new RawEmailInput("msg1", "thread1", Map.of(), List.of(), null, from, List.of(), "subj", null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullFrom() {
        assertThatThrownBy(() -> new RawEmailInput("msg1", "thread1", Map.of(), List.of(), Instant.now(), null, List.of(), "subj", null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullSubject() {
        assertThatThrownBy(() -> new RawEmailInput("msg1", "thread1", Map.of(), List.of(), Instant.now(), from, List.of(), null, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void defaultsNullHeadersToEmptyMap() {
        RawEmailInput raw = new RawEmailInput("msg1", "thread1", null, List.of(), Instant.now(), from, List.of(), "subj", null, null);
        assertThat(raw.headers()).isEmpty();
    }

    @Test
    void defaultsNullPartsToEmptyList() {
        RawEmailInput raw = new RawEmailInput("msg1", "thread1", Map.of(), null, Instant.now(), from, List.of(), "subj", null, null);
        assertThat(raw.parts()).isEmpty();
    }

    @Test
    void defaultsNullToToEmptyList() {
        RawEmailInput raw = new RawEmailInput("msg1", "thread1", Map.of(), List.of(), Instant.now(), from, null, "subj", null, null);
        assertThat(raw.to()).isEmpty();
    }

    @Test
    void makesDefensiveCopyOfHeaders() {
        Map<String, String> original = new java.util.HashMap<>();
        original.put("from", "a@b.com");
        RawEmailInput raw = new RawEmailInput("msg1", "thread1", original, List.of(), Instant.now(), from, List.of(), "subj", null, null);
        original.put("extra", "value");
        assertThat(raw.headers()).doesNotContainKey("extra");
    }

    @Test
    void makesDefensiveCopyOfParts() {
        List<RawEmailInput.PartInput> original = new java.util.ArrayList<>();
        original.add(new RawEmailInput.PartInput("text/plain", null, null, "body", null, 0));
        RawEmailInput raw = new RawEmailInput("msg1", "thread1", Map.of(), original, Instant.now(), from, List.of(), "subj", null, null);
        original.add(new RawEmailInput.PartInput("text/html", null, null, "<html>", null, 0));
        assertThat(raw.parts()).hasSize(1);
    }

    @Test
    void preservesProvidedValues() {
        Instant now = Instant.now();
        List<EmailAddress> to = List.of(new EmailAddress("recip@x.com"));
        RawEmailInput raw = new RawEmailInput("msg1", "thread1", Map.of("k", "v"), List.of(), now, from, to, "Hello", "bodyText", "<p>bodyHtml</p>");
        assertThat(raw.gmailMessageId()).isEqualTo("msg1");
        assertThat(raw.gmailThreadId()).isEqualTo("thread1");
        assertThat(raw.sentAt()).isEqualTo(now);
        assertThat(raw.from()).isEqualTo(from);
        assertThat(raw.to()).containsExactly(new EmailAddress("recip@x.com"));
        assertThat(raw.subject()).isEqualTo("Hello");
        assertThat(raw.bodyText()).isEqualTo("bodyText");
        assertThat(raw.bodyHtml()).isEqualTo("<p>bodyHtml</p>");
    }

    @Test
    void partInput_rejectsNullMimeType() {
        assertThatThrownBy(() -> new RawEmailInput.PartInput(null, null, null, null, null, 0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void partInput_defaultsNullFilenameToEmpty() {
        RawEmailInput.PartInput part = new RawEmailInput.PartInput("text/plain", null, null, "body", null, 0);
        assertThat(part.filename()).isEmpty();
    }

    @Test
    void partInput_clampsNegativeSizeToZero() {
        RawEmailInput.PartInput part = new RawEmailInput.PartInput("text/plain", null, null, null, null, -100);
        assertThat(part.sizeBytes()).isZero();
    }

    @Test
    void partInput_isAttachment_whenGmailAttachmentIdPresent() {
        RawEmailInput.PartInput part = new RawEmailInput.PartInput("application/pdf", null, "doc.pdf", null, "att123", 100);
        assertThat(part.isAttachment()).isTrue();
    }

    @Test
    void partInput_isAttachment_whenContentDispositionContainsAttachment() {
        RawEmailInput.PartInput part = new RawEmailInput.PartInput("application/pdf", "attachment; filename=doc.pdf", "doc.pdf", null, null, 100);
        assertThat(part.isAttachment()).isTrue();
    }

    @Test
    void partInput_isNotAttachment_whenNoAttachmentIdAndNoDisposition() {
        RawEmailInput.PartInput part = new RawEmailInput.PartInput("text/plain", null, null, "body", null, 0);
        assertThat(part.isAttachment()).isFalse();
    }

    @Test
    void partInput_isNotAttachment_whenDispositionIsInline() {
        RawEmailInput.PartInput part = new RawEmailInput.PartInput("text/plain", "inline", null, "body", null, 0);
        assertThat(part.isAttachment()).isFalse();
    }

    @Test
    void partInput_isAttachment_withCaseInsensitiveCheck() {
        RawEmailInput.PartInput part = new RawEmailInput.PartInput("application/pdf", "ATTACHMENT", "doc.pdf", null, null, 100);
        assertThat(part.isAttachment()).isTrue();
    }
}
