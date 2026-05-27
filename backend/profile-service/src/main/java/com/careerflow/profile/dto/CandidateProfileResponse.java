/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CandidateProfileResponse(
        UUID id,
        String fullName,
        String professionalTitle,
        String email,
        String phone,
        String location,
        String summary,
        List<SkillResponse> skills,
        List<ExperienceResponse> experiences,
        Instant createdAt,
        Instant updatedAt
) {
}
