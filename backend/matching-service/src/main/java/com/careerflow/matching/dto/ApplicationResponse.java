package com.careerflow.matching.dto;

import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID profileId,
        UUID jobId,
        ApplicationStatus status,
        String notes,
        Instant appliedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
