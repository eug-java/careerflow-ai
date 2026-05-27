/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.matching.service;

import com.careerflow.matching.client.JobResponse;
import com.careerflow.matching.client.ProfileResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MatchingScoreCalculator {

    public MatchScore calculate(ProfileResponse profile, JobResponse job) {
        BigDecimal skillsScore = calculateSkillsScore(profile, job);
        BigDecimal locationScore = calculateLocationScore(profile, job);
        BigDecimal salaryScore = calculateSalaryScore(job);

        BigDecimal totalScore = skillsScore.multiply(BigDecimal.valueOf(0.70))
                .add(locationScore.multiply(BigDecimal.valueOf(0.20)))
                .add(salaryScore.multiply(BigDecimal.valueOf(0.10)))
                .setScale(2, RoundingMode.HALF_UP);

        String explanation = """
                Skills score: %s
                Location score: %s
                Salary score: %s
                Total score formula: skills 70%% + location 20%% + salary 10%%
                """.formatted(skillsScore, locationScore, salaryScore);

        return new MatchScore(totalScore, skillsScore, locationScore, salaryScore, explanation);
    }

    private BigDecimal calculateSkillsScore(ProfileResponse profile, JobResponse job) {
        if (job.skills() == null || job.skills().isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<String> candidateSkills = profile.skills() == null
                ? Set.of()
                : profile.skills().stream()
                    .map(skill -> normalize(skill.name()))
                    .collect(Collectors.toSet());

        long matchedRequiredSkills = job.skills().stream()
                .filter(JobResponse.JobSkillResponse::required)
                .filter(skill -> candidateSkills.contains(normalize(skill.name())))
                .count();

        long requiredSkills = job.skills().stream()
                .filter(JobResponse.JobSkillResponse::required)
                .count();

        if (requiredSkills == 0) {
            return BigDecimal.valueOf(100);
        }

        return BigDecimal.valueOf(matchedRequiredSkills)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(requiredSkills), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLocationScore(ProfileResponse profile, JobResponse job) {
        if (Boolean.TRUE.equals(job.remote())) {
            return BigDecimal.valueOf(100);
        }

        if (profile.location() == null || job.location() == null) {
            return BigDecimal.valueOf(50);
        }

        String profileLocation = normalize(profile.location());
        String jobLocation = normalize(job.location());

        if (profileLocation.contains(jobLocation) || jobLocation.contains(profileLocation)) {
            return BigDecimal.valueOf(100);
        }

        if (profileLocation.contains("austin") && jobLocation.contains("austin")) {
            return BigDecimal.valueOf(100);
        }

        if (profileLocation.contains("tx") && jobLocation.contains("tx")) {
            return BigDecimal.valueOf(70);
        }

        return BigDecimal.valueOf(30);
    }

    private BigDecimal calculateSalaryScore(JobResponse job) {
        if (job.salaryMin() == null && job.salaryMax() == null) {
            return BigDecimal.valueOf(50);
        }

        return BigDecimal.valueOf(100);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public record MatchScore(
            BigDecimal totalScore,
            BigDecimal skillsScore,
            BigDecimal locationScore,
            BigDecimal salaryScore,
            String explanation
    ) {
    }
}
