package com.jobtracker.emailmanagement.domain.model;

import java.util.Objects;

public record ClassificationResult(
        String classification,
        String serializedResult,
        Double score,
        String confidence
) {
    public ClassificationResult {
        Objects.requireNonNull(classification, "classification must not be null");
        if (score != null && (score < 0.0 || score > 1.0))
            throw new IllegalArgumentException("score must be between 0.0 and 1.0, got: " + score);
    }
}
