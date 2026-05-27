/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record SkillRequest(
        @NotBlank String name,
        String category,
        BigDecimal yearsOfExperience
) {
}
