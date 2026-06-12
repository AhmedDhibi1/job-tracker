package com.jobtracker.emailmanagement.infrastructure.gmail.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawGmailMessageTest {

    @Test
    void rejectsNullGmailMessageId() {
        assertThatThrownBy(() -> new RawGmailMessage(null, "t1", Map.of(), List.of(), Instant.now(), "from@x.com", List.of(), "subj", null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullGmailThreadId() {
        assertThatThrownBy(() -> new RawGmailMessage("m1", null, Map.of(), List.of(), Instant.now(), "from@x.com", List.of(), "subj", null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullSentAt() {
        assertThatThrownBy(() -> new RawGmailMessage("m1", "t1", Map.of(), List.of(), null, "from@x.com", List.of(), "subj", null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullFrom() {
        assertThatThrownBy(() -> new RawGmailMessage("m1", "t1", Map.of(), List.of(), Instant.now(), null, List.of(), "subj", null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullSubject() {
        assertThatThrownBy(() -> new RawGmailMessage("m1", "t1", Map.of(), List.of(), Instant.now(), "from@x.com", List.of(), null, null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void defaultsNullFields() {
        RawGmailMessage msg = new RawGmailMessage("m1", "t1", null, null, Instant.now(), "from@x.com", null, "subj", null, null, null);
        assertThat(msg.headers()).isEmpty();
        assertThat(msg.parts()).isEmpty();
        assertThat(msg.to()).isEmpty();
        assertThat(msg.accountEmailAddresses()).isEmpty();
    }

    @Test
    void mimePart_rejectsNullMimeType() {
        assertThatThrownBy(() -> new RawGmailMessage.MimePart(null, null, null, null, null, 0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void mimePart_defaultsNullFilenameToEmpty() {
        RawGmailMessage.MimePart part = new RawGmailMessage.MimePart("text/plain", null, null, null, null, 0);
        assertThat(part.filename()).isEmpty();
    }

    @Test
    void mimePart_clampsNegativeSizeToZero() {
        RawGmailMessage.MimePart part = new RawGmailMessage.MimePart("text/plain", null, null, null, null, -100);
        assertThat(part.sizeBytes()).isZero();
    }

    @Test
    void mimePart_isAttachment_withGmailAttachmentId() {
        RawGmailMessage.MimePart part = new RawGmailMessage.MimePart("application/pdf", null, "doc.pdf", null, "att123", 100);
        assertThat(part.isAttachment()).isTrue();
    }

    @Test
    void mimePart_isAttachment_withDisposition() {
        RawGmailMessage.MimePart part = new RawGmailMessage.MimePart("application/pdf", "attachment", "doc.pdf", null, null, 100);
        assertThat(part.isAttachment()).isTrue();
    }

    @Test
    void mimePart_isNotAttachment_whenNoIndication() {
        RawGmailMessage.MimePart part = new RawGmailMessage.MimePart("text/plain", null, null, "body", null, 0);
        assertThat(part.isAttachment()).isFalse();
    }
}
