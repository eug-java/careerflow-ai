/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.client;

import java.util.UUID;

public record GenerateContentResponse(UUID profileId, UUID jobId, String documentType, String generationMode, String model, String content) {}
