/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.client;

import java.util.UUID;

public record GenerateContentRequest(UUID profileId, UUID jobId, String documentType) {}
