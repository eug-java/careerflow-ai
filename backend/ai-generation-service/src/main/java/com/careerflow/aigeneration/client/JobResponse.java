/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record JobResponse(
        UUID id,
        String title,
        String companyName,
        String location,
        String employmentType,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        Boolean remote,
        String description,
        List<JobSkillResponse> skills
) {
    public record JobSkillResponse(
            UUID id,
            String name,
            boolean required
    ) {
    }
}
