/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentGeneratedEvent(
        UUID eventId,
        UUID profileId,
        UUID jobId,
        String documentType,
        String content,
        Instant generatedAt
) {
}
