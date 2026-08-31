package com.careerflow.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record StartBatchDocumentGenerationRequest(
        @NotNull UUID profileId,
        @NotEmpty @Size(max = 20) List<UUID> jobIds,
        @NotBlank String documentType
) {
}
