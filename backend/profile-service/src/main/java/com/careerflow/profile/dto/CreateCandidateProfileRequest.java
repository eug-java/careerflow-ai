/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateCandidateProfileRequest(
        @NotBlank String fullName,
        String professionalTitle,
        @Email String email,
        String phone,
        String location,
        String summary,

        @Valid List<SkillRequest> skills,
        @Valid List<ExperienceRequest> experiences
) {
}
