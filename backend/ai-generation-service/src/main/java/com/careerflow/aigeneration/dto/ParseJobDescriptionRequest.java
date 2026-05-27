/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.dto;

import jakarta.validation.constraints.NotBlank;

public record ParseJobDescriptionRequest(
        @NotBlank String text
) {
}
