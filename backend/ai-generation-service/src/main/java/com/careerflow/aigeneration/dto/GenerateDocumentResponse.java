/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.dto;

import com.careerflow.aigeneration.client.DocumentResponse;

import java.time.Instant;
import java.util.UUID;

public record GenerateDocumentResponse(
        UUID profileId,
        UUID jobId,
        DocumentType documentType,
        String generationMode,
        String model,
        String content,
        DocumentResponse savedDocument,
        Instant generatedAt
) {
}
