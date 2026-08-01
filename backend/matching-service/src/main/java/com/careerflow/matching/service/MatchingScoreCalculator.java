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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MatchingScoreCalculator {

    private final LocationMatcher locationMatcher;

    public MatchingScoreCalculator(LocationMatcher locationMatcher) {
        this.locationMatcher = locationMatcher;
    }

    public MatchScore calculate(ProfileResponse profile, JobResponse job) {
        BigDecimal skillsScore = calculateSkillsScore(profile, job);
        BigDecimal locationScore = calculateLocationScore(profile, job);
        BigDecimal experienceScore = calculateExperienceScore(profile, job);
        BigDecimal salaryScore = calculateSalaryScore(job);

        BigDecimal totalScore = skillsScore.multiply(BigDecimal.valueOf(0.45))
                .add(locationScore.multiply(BigDecimal.valueOf(0.25)))
                .add(experienceScore.multiply(BigDecimal.valueOf(0.20)))
                .add(salaryScore.multiply(BigDecimal.valueOf(0.10)))
                .setScale(2, RoundingMode.HALF_UP);

        String explanation = """
                Skills score: %s
                Location score: %s
                Experience score: %s
                Salary score: %s
                Total score formula: skills 45%% + location 25%% + experience 20%% + salary 10%%
                """.formatted(skillsScore, locationScore, experienceScore, salaryScore);

        return new MatchScore(totalScore, skillsScore, locationScore, experienceScore, salaryScore, explanation);
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

        long matchedRequired = job.skills().stream()
                .filter(JobResponse.JobSkillResponse::required)
                .filter(skill -> candidateSkills.contains(normalize(skill.name())))
                .count();

        long requiredSkills = job.skills().stream()
                .filter(JobResponse.JobSkillResponse::required)
                .count();

        long matchedOptional = job.skills().stream()
                .filter(skill -> !skill.required())
                .filter(skill -> candidateSkills.contains(normalize(skill.name())))
                .count();

        long optionalSkills = job.skills().stream()
                .filter(skill -> !skill.required())
                .count();

        BigDecimal requiredPart = requiredSkills == 0
                ? BigDecimal.valueOf(100)
                : BigDecimal.valueOf(matchedRequired)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(requiredSkills), 2, RoundingMode.HALF_UP);

        BigDecimal optionalPart = optionalSkills == 0
                ? BigDecimal.valueOf(100)
                : BigDecimal.valueOf(matchedOptional)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(optionalSkills), 2, RoundingMode.HALF_UP);

        return requiredPart.multiply(BigDecimal.valueOf(0.80))
                .add(optionalPart.multiply(BigDecimal.valueOf(0.20)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLocationScore(ProfileResponse profile, JobResponse job) {
        String preference = profile.locationPreference() == null ? "CITY" : profile.locationPreference();
        int score = locationMatcher.score(
                profile.location(),
                preference,
                job.location(),
                Boolean.TRUE.equals(job.remote())
        );
        return BigDecimal.valueOf(score);
    }

    private BigDecimal calculateExperienceScore(ProfileResponse profile, JobResponse job) {
        double totalYears = totalExperienceYears(profile);
        double expectedYears = expectedYearsForJob(job);

        if (expectedYears <= 0) {
            return BigDecimal.valueOf(totalYears > 0 ? 100 : 50);
        }

        double ratio = totalYears / expectedYears;
        int score = (int) Math.min(100, Math.round(ratio * 100));
        if (totalYears >= expectedYears) {
            score = Math.max(score, 85);
        }
        return BigDecimal.valueOf(score);
    }

    private double totalExperienceYears(ProfileResponse profile) {
        double fromExperiences = 0;
        if (profile.experiences() != null) {
            for (ProfileResponse.ExperienceResponse experience : profile.experiences()) {
                fromExperiences += yearsBetween(experience.startDate(), experience.endDate(), experience.currentPosition());
            }
        }

        double fromSkills = 0;
        if (profile.skills() != null) {
            fromSkills = profile.skills().stream()
                    .map(skill -> skill.yearsOfExperience() == null ? BigDecimal.ZERO : skill.yearsOfExperience())
                    .mapToDouble(BigDecimal::doubleValue)
                    .max()
                    .orElse(0);
        }

        return Math.max(fromExperiences, fromSkills);
    }

    private double yearsBetween(LocalDate start, LocalDate end, Boolean current) {
        if (start == null) {
            return 0;
        }
        LocalDate effectiveEnd = Boolean.TRUE.equals(current) || end == null ? LocalDate.now() : end;
        long months = ChronoUnit.MONTHS.between(start.withDayOfMonth(1), effectiveEnd.withDayOfMonth(1));
        return Math.max(0, months / 12.0);
    }

    private double expectedYearsForJob(JobResponse job) {
        String title = normalize(job.title());
        if (title.contains("intern") || title.contains("entry") || title.contains("junior")) {
            return 1.5;
        }
        if (title.contains("senior") || title.contains("sr ")) {
            return 5;
        }
        if (title.contains("lead") || title.contains("principal") || title.contains("staff")) {
            return 8;
        }
        if (title.contains("director") || title.contains("head")) {
            return 10;
        }
        return 3;
    }

    private BigDecimal calculateSalaryScore(JobResponse job) {
        if (job.salaryMin() == null && job.salaryMax() == null) {
            return BigDecimal.valueOf(50);
        }
        return BigDecimal.valueOf(100);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record MatchScore(
            BigDecimal totalScore,
            BigDecimal skillsScore,
            BigDecimal locationScore,
            BigDecimal experienceScore,
            BigDecimal salaryScore,
            String explanation
    ) {
    }
}
