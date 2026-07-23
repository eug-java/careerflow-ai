/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.matching.service;

import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.security.CurrentUserProvider;
import com.careerflow.common.security.InternalAuthSupport;
import com.careerflow.matching.client.JobClient;
import com.careerflow.matching.client.JobResponse;
import com.careerflow.matching.client.ProfileClient;
import com.careerflow.matching.client.ProfileResponse;
import com.careerflow.matching.dto.CreateMatchRequest;
import com.careerflow.matching.dto.MatchResultResponse;
import com.careerflow.matching.entity.JobMatchResult;
import com.careerflow.matching.repository.JobMatchResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class MatchingService {

    private final ProfileClient profileClient;
    private final JobClient jobClient;
    private final MatchingScoreCalculator scoreCalculator;
    private final JobMatchResultRepository repository;

    public MatchingService(
            ProfileClient profileClient,
            JobClient jobClient,
            MatchingScoreCalculator scoreCalculator,
            JobMatchResultRepository repository
    ) {
        this.profileClient = profileClient;
        this.jobClient = jobClient;
        this.scoreCalculator = scoreCalculator;
        this.repository = repository;
    }

    @Transactional
    public MatchResultResponse calculate(CreateMatchRequest request) {
        ProfileResponse profile = profileClient.getProfile(request.profileId());
        JobResponse job = jobClient.getJob(request.jobId());

        MatchingScoreCalculator.MatchScore score = scoreCalculator.calculate(profile, job);

        JobMatchResult result = new JobMatchResult();
        result.setProfileId(request.profileId());
        result.setJobId(request.jobId());
        result.setTotalScore(score.totalScore());
        result.setSkillsScore(score.skillsScore());
        result.setLocationScore(score.locationScore());
        result.setSalaryScore(score.salaryScore());
        result.setExplanation(score.explanation());
        if (!InternalAuthSupport.isInternalCall()) {
            result.setOwnerId(CurrentUserProvider.requireUserId());
        }

        return toResponse(repository.save(result));
    }

    private MatchResultResponse toResponse(JobMatchResult result) {
        return new MatchResultResponse(
                result.getId(),
                result.getProfileId(),
                result.getJobId(),
                result.getTotalScore(),
                result.getSkillsScore(),
                result.getLocationScore(),
                result.getSalaryScore(),
                result.getExplanation(),
                result.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public MatchResultResponse findById(UUID id) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        JobMatchResult result = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Match result not found: " + id));

        return toResponse(result);
    }

    @Transactional(readOnly = true)
    public List<MatchResultResponse> findAll(UUID profileId, UUID jobId) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        if (profileId != null) {
            return repository.findByOwnerIdAndProfileId(ownerId, profileId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        if (jobId != null) {
            return repository.findByOwnerIdAndJobId(ownerId, jobId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return repository.findByOwnerId(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}
