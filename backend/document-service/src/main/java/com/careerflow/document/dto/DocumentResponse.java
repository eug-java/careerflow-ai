/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.document.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID profileId,
        UUID jobId,
        String documentType,
        String fileName,
        String contentType,
        String storageBucket,
        String storageKey,
        Instant createdAt
) {
}
