/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SaveGeneratedDocumentRequest(
        @NotNull UUID profileId,
        @NotNull UUID jobId,
        @NotBlank String documentType,
        @NotBlank String content
) {
}
