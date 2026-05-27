/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartDocumentGenerationWorkflowRequest(
        @NotNull UUID profileId,
        @NotNull UUID jobId,
        @NotBlank String documentType
) {
}
