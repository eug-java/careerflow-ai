/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.exception;

import com.careerflow.common.api.ResourceNotFoundException;

import java.util.UUID;

public class ProfileNotFoundException extends ResourceNotFoundException {

    public ProfileNotFoundException(UUID id) {
        super("Candidate profile not found: " + id);
    }
}
