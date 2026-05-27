/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.matching.client;

import java.math.BigDecimal;
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
        List<SkillResponse> skills
) {
    public record SkillResponse(
            UUID id,
            String name,
            String category,
            BigDecimal yearsOfExperience
    ) {
    }
}
