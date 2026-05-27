/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.dto;

import java.time.LocalDate;
import java.util.UUID;

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
