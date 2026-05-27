/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateDocumentRequest(
        @NotNull UUID profileId,
        @NotNull UUID jobId,
        @NotNull DocumentType documentType
) {
}
