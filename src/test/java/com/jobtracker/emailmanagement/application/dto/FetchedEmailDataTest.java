package com.jobtracker.emailmanagement.application.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FetchedEmailDataTest {

    private final Instant now = Instant.now();

    @Test
    void rejectsNullGmailMessageId() {
        assertThatThrownBy(() -> new FetchedEmailData(null, "t1", Map.of(), List.of(), now, "from@x.com", List.of(), "subj", null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullGmailThreadId() {
        assertThatThrownBy(() -> new FetchedEmailData("m1", null, Map.of(), List.of(), now, "from@x.com", List.of(), "subj", null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullSentAt() {
        assertThatThrownBy(() -> new FetchedEmailData("m1", "t1", Map.of(), List.of(), null, "from@x.com", List.of(), "subj", null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullFrom() {
        assertThatThrownBy(() -> new FetchedEmailData("m1", "t1", Map.of(), List.of(), now, null, List.of(), "subj", null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullSubject() {
        assertThatThrownBy(() -> new FetchedEmailData("m1", "t1", Map.of(), List.of(), now, "from@x.com", List.of(), null, null, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void defaultsNullHeadersToEmptyMap() {
        FetchedEmailData data = new FetchedEmailData("m1", "t1", null, List.of(), now, "from@x.com", List.of(), "subj", null, null, List.of());
        assertThat(data.headers()).isEmpty();
    }

    @Test
    void defaultsNullAttachmentsToEmptyList() {
        FetchedEmailData data = new FetchedEmailData("m1", "t1", Map.of(), null, now, "from@x.com", List.of(), "subj", null, null, List.of());
        assertThat(data.attachments()).isEmpty();
    }

    @Test
    void defaultsNullToToEmptyList() {
        FetchedEmailData data = new FetchedEmailData("m1", "t1", Map.of(), List.of(), now, "from@x.com", null, "subj", null, null, List.of());
        assertThat(data.to()).isEmpty();
    }

    @Test
    void defaultsNullAccountEmailAddressesToEmptyList() {
        FetchedEmailData data = new FetchedEmailData("m1", "t1", Map.of(), List.of(), now, "from@x.com", List.of(), "subj", null, null, null);
        assertThat(data.accountEmailAddresses()).isEmpty();
    }

    @Test
    void preservesValues() {
        FetchedEmailData data = new FetchedEmailData("m1", "t1", Map.of("k", "v"), List.of(), now, "from@x.com", List.of("to@y.com"), "Subj", "body", "<html>", List.of("me@x.com"));
        assertThat(data.gmailMessageId()).isEqualTo("m1");
        assertThat(data.subject()).isEqualTo("Subj");
        assertThat(data.bodyText()).isEqualTo("body");
        assertThat(data.bodyHtml()).isEqualTo("<html>");
    }

    @Test
    void attachmentPart_rejectsNullMimeType() {
        assertThatThrownBy(() -> new FetchedEmailData.AttachmentPart(null, null, null, null, null, 0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void attachmentPart_defaultsNullFilenameToEmpty() {
        FetchedEmailData.AttachmentPart part = new FetchedEmailData.AttachmentPart("text/plain", null, null, null, null, 0);
        assertThat(part.filename()).isEmpty();
    }

    @Test
    void attachmentPart_clampsNegativeSizeToZero() {
        FetchedEmailData.AttachmentPart part = new FetchedEmailData.AttachmentPart("text/plain", null, null, null, null, -50);
        assertThat(part.sizeBytes()).isZero();
    }

    @Test
    void attachmentPart_isAttachment_withGmailAttachmentId() {
        FetchedEmailData.AttachmentPart part = new FetchedEmailData.AttachmentPart("application/pdf", null, "doc.pdf", null, "att123", 100);
        assertThat(part.isAttachment()).isTrue();
    }

    @Test
    void attachmentPart_isAttachment_withDisposition() {
        FetchedEmailData.AttachmentPart part = new FetchedEmailData.AttachmentPart("application/pdf", "attachment", "doc.pdf", null, null, 100);
        assertThat(part.isAttachment()).isTrue();
    }

    @Test
    void attachmentPart_isNotAttachment_whenNoIndication() {
        FetchedEmailData.AttachmentPart part = new FetchedEmailData.AttachmentPart("text/plain", null, null, "body", null, 0);
        assertThat(part.isAttachment()).isFalse();
    }

    @Test
    void makesDefensiveCopyOfHeaders() {
        Map<String, String> mutable = new java.util.HashMap<>();
        mutable.put("k", "v");
        FetchedEmailData data = new FetchedEmailData("m1", "t1", mutable, List.of(), now, "from@x.com", List.of(), "subj", null, null, List.of());
        mutable.put("k2", "v2");
        assertThat(data.headers()).hasSize(1);
    }
}
