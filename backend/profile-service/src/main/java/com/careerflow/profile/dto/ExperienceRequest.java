/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ExperienceRequest(
        @NotBlank String companyName,
        @NotBlank String positionTitle,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        boolean currentPosition,
        String description
) {
}
