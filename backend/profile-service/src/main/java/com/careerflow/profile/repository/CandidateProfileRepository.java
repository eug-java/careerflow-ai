/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.repository;

import com.careerflow.profile.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {
}
