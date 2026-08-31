package com.careerflow.matching.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateApplicationRequest(
        @NotNull UUID profileId,
        @NotNull UUID jobId,
        ApplicationStatus status,
        String notes
) {
}
