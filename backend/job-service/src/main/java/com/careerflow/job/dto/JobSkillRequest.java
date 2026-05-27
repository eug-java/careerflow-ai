/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.job.dto;

public record JobSkillRequest(
        String name,
        boolean required
) {}
