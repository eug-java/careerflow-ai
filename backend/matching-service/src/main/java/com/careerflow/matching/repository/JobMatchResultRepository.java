/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.matching.repository;

import com.careerflow.matching.entity.JobMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobMatchResultRepository extends JpaRepository<JobMatchResult, UUID> {

    List<JobMatchResult> findByProfileId(UUID profileId);

    List<JobMatchResult> findByJobId(UUID jobId);
}
