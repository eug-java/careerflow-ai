/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.matching.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchResultResponse(
        UUID id,
        UUID profileId,
        UUID jobId,
        BigDecimal totalScore,
        BigDecimal skillsScore,
        BigDecimal locationScore,
        BigDecimal salaryScore,
        String explanation,
        Instant createdAt
) {
}
