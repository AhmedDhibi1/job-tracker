package com.jobtracker.emailmanagement.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClassificationResultTest {

    @Test
    void acceptsValidScoreWithinBounds() {
        ClassificationResult r = new ClassificationResult("APPLICATION", "{}", 0.5, "MEDIUM");
        assertThat(r.classification()).isEqualTo("APPLICATION");
        assertThat(r.score()).isEqualTo(0.5);
    }

    @Test
    void acceptsNullScore() {
        ClassificationResult r = new ClassificationResult("SPAM", null, null, "LOW");
        assertThat(r.score()).isNull();
    }

    @Test
    void acceptsNullConfidence() {
        ClassificationResult r = new ClassificationResult("APPLICATION", null, 0.5, null);
        assertThat(r.confidence()).isNull();
    }

    @Test
    void acceptsNullSerializedResult() {
        ClassificationResult r = new ClassificationResult("APPLICATION", null, 0.5, "HIGH");
        assertThat(r.serializedResult()).isNull();
    }

    @Test
    void rejectsNullClassification() {
        assertThatThrownBy(() -> new ClassificationResult(null, "{}", 0.5, "HIGH"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("classification");
    }

    @Test
    void rejectsScoreBelowZero() {
        assertThatThrownBy(() -> new ClassificationResult("APPLICATION", "{}", -0.1, "HIGH"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("score must be between 0.0 and 1.0");
    }

    @Test
    void rejectsScoreAboveOne() {
        assertThatThrownBy(() -> new ClassificationResult("APPLICATION", "{}", 1.1, "HIGH"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("score must be between 0.0 and 1.0");
    }

    @Test
    void acceptsBoundaryScores() {
        ClassificationResult low = new ClassificationResult("X", null, 0.0, null);
        ClassificationResult high = new ClassificationResult("X", null, 1.0, null);
        assertThat(low.score()).isZero();
        assertThat(high.score()).isEqualTo(1.0);
    }
}
