/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String fullName,
        String professionalTitle,
        String email,
        String phone,
        String location,
        String summary,
        List<SkillResponse> skills,
        List<ExperienceResponse> experiences
) {
    public record SkillResponse(
            UUID id,
            String name,
            String category,
            BigDecimal yearsOfExperience
    ) {
    }

    public record ExperienceResponse(
            UUID id,
            String companyName,
            String positionTitle,
            String location,
            LocalDate startDate,
            LocalDate endDate,
            boolean currentPosition,
            String description
    ) {
    }
}
