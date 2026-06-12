package com.jobtracker.emailmanagement.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAttachmentMetadataTest {

    @Test
    void rejectsNullFilename() {
        assertThatThrownBy(() -> new EmailAttachmentMetadata(null, "text/plain", 100L, "att1"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("filename");
    }

    @Test
    void rejectsNullMimeType() {
        assertThatThrownBy(() -> new EmailAttachmentMetadata("resume.pdf", null, 100L, "att1"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("mimeType");
    }

    @Test
    void rejectsNegativeSizeBytes() {
        assertThatThrownBy(() -> new EmailAttachmentMetadata("file.txt", "text/plain", -1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sizeBytes");
    }

    @Test
    void acceptsZeroSize() {
        EmailAttachmentMetadata meta = new EmailAttachmentMetadata("empty.txt", "text/plain", 0L, null);
        assertThat(meta.sizeBytes()).isZero();
    }

    @Test
    void hasAttachmentId_returnsTrueWhenPresent() {
        EmailAttachmentMetadata meta = new EmailAttachmentMetadata("f", "t", 1L, "att123");
        assertThat(meta.hasAttachmentId()).isTrue();
    }

    @Test
    void hasAttachmentId_returnsFalseWhenNull() {
        EmailAttachmentMetadata meta = new EmailAttachmentMetadata("f", "t", 1L, null);
        assertThat(meta.hasAttachmentId()).isFalse();
    }

    @Test
    void hasAttachmentId_returnsFalseWhenBlank() {
        EmailAttachmentMetadata meta = new EmailAttachmentMetadata("f", "t", 1L, "   ");
        assertThat(meta.hasAttachmentId()).isFalse();
    }

    @Test
    void preservesAllFields() {
        EmailAttachmentMetadata meta = new EmailAttachmentMetadata("report.pdf", "application/pdf", 2048L, "att456");
        assertThat(meta.filename()).isEqualTo("report.pdf");
        assertThat(meta.mimeType()).isEqualTo("application/pdf");
        assertThat(meta.sizeBytes()).isEqualTo(2048L);
        assertThat(meta.gmailAttachmentId()).isEqualTo("att456");
    }
}
