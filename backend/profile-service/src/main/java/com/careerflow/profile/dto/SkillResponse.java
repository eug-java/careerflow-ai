/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SkillResponse(
        UUID id,
        String name,
        String category,
        BigDecimal yearsOfExperience
) {
}
