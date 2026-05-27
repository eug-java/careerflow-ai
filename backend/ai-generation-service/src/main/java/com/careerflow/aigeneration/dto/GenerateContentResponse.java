/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.dto;

import java.util.UUID;

public record GenerateContentResponse(
        UUID profileId,
        UUID jobId,
        DocumentType documentType,
        String generationMode,
        String model,
        String content
) {
}
