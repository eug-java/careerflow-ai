/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.job.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateJobRequest(
        String title,
        String companyName,
        String location,
        String employmentType,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        Boolean remote,
        String description,
        List<JobSkillRequest> skills
) {}
