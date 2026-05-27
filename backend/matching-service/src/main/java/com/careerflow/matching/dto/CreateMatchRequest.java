/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.matching.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMatchRequest(
        @NotNull UUID profileId,
        @NotNull UUID jobId
) {
}
