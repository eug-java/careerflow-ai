/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.job.dto;

import java.util.UUID;

public record JobSkillResponse(
        UUID id,
        String name,
        boolean required
) {
}
