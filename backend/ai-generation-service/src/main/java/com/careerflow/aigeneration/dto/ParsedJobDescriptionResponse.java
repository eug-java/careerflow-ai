/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.dto;

import java.util.List;

public record ParsedJobDescriptionResponse(
        String title,
        String companyName,
        String location,
        String employmentType,
        Double salaryMin,
        Double salaryMax,
        String currency,
        Boolean remote,
        String description,
        List<ParsedJobSkillResponse> skills
) {
}
