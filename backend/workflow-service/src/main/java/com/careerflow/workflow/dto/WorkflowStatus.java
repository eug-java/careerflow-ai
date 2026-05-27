/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.dto;

public record WorkflowStatus(
        long processInstanceKey, String processId, String status, String message) {}
