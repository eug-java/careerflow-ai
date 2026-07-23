/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.matching.repository;

import com.careerflow.matching.entity.JobMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobMatchResultRepository extends JpaRepository<JobMatchResult, UUID> {

    Optional<JobMatchResult> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<JobMatchResult> findByOwnerId(UUID ownerId);

    List<JobMatchResult> findByOwnerIdAndProfileId(UUID ownerId, UUID profileId);

    List<JobMatchResult> findByOwnerIdAndJobId(UUID ownerId, UUID jobId);
}
