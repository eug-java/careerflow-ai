/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.client;

import java.util.UUID;

public record SaveDocumentRequest(
        UUID profileId,
        UUID jobId,
        String documentType,
        String content
) {
}
