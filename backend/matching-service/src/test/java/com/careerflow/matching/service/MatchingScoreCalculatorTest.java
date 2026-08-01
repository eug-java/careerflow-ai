package com.careerflow.matching.service;

import com.careerflow.matching.client.JobResponse;
import com.careerflow.matching.client.ProfileResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingScoreCalculatorTest {

    private final MatchingScoreCalculator calculator = new MatchingScoreCalculator(new LocationMatcher());

    @Test
    void calculateReturnsStrongScoreForRemoteJobWithRequiredSkills() {
        ProfileResponse profile = profileWithSkills("Java", "Spring Boot", "Kafka");
        JobResponse job = jobWithSkills(true, "Austin, TX", 120_000.0, List.of(
                requiredSkill("java"),
                requiredSkill("Spring Boot"),
                optionalSkill("React")
        ));

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.skillsScore()).isEqualByComparingTo("80.00");
        assertThat(score.locationScore()).isEqualByComparingTo("100");
        assertThat(score.experienceScore()).isNotNull();
        assertThat(score.totalScore()).isGreaterThan(BigDecimal.valueOf(70));
    }

    @Test
    void calculateUsesOnlyRequiredSkillsForSkillScore() {
        ProfileResponse profile = profileWithSkills("Java");
        JobResponse job = jobWithSkills(false, "Austin, TX", null, List.of(
                requiredSkill("Java"),
                requiredSkill("Kafka"),
                optionalSkill("Docker")
        ));

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.skillsScore()).isEqualByComparingTo("40.00");
        assertThat(score.locationScore()).isEqualByComparingTo("100");
    }

    @Test
    void calculateReturnsZeroSkillsScoreWhenJobHasNoSkills() {
        ProfileResponse profile = profileWithSkills("Java");
        JobResponse job = jobWithSkills(false, "Dallas, TX", null, List.of());

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.skillsScore()).isEqualByComparingTo("0");
        assertThat(score.locationScore()).isEqualByComparingTo("75");
    }

    @Test
    void calculateReturnsHighScoreForNationwidePreference() {
        ProfileResponse profile = new ProfileResponse(
                UUID.randomUUID(), "Name", "Engineer", "a@b.com", null,
                "Open to relocation anywhere in USA", "NATIONWIDE", null,
                List.of(), List.of()
        );
        JobResponse job = jobWithSkills(false, "Seattle, WA", null, List.of(requiredSkill("Java")));

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.locationScore()).isEqualByComparingTo("95");
    }

    @Test
    void calculateUsesExperienceYearsFromWorkHistory() {
        ProfileResponse profile = new ProfileResponse(
                UUID.randomUUID(), "Jane", "Senior Java Developer", "jane@example.com", null,
                "Austin, TX", "CITY", null,
                List.of(new ProfileResponse.SkillResponse(UUID.randomUUID(), "Java", "backend", BigDecimal.valueOf(6))),
                List.of(new ProfileResponse.ExperienceResponse(
                        UUID.randomUUID(), "Acme", "Senior Engineer", "Austin, TX",
                        LocalDate.of(2018, 1, 1), null, true, "Built microservices"
                ))
        );
        JobResponse job = new JobResponse(
                UUID.randomUUID(), "Senior Java Developer", "CareerFlow", "Austin, TX",
                "FULL_TIME", 120_000.0, 150_000.0, "USD", false, "Backend role",
                List.of(requiredSkill("Java"))
        );

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.experienceScore()).isGreaterThanOrEqualTo(BigDecimal.valueOf(85));
    }

    private static ProfileResponse profileWithSkills(String... skills) {
        List<ProfileResponse.SkillResponse> skillResponses = java.util.Arrays.stream(skills)
                .map(skill -> new ProfileResponse.SkillResponse(UUID.randomUUID(), skill, "backend", BigDecimal.ONE))
                .toList();
        return new ProfileResponse(
                UUID.randomUUID(), "Jane Doe", "Java Developer", "jane@example.com", null,
                "Austin, TX", "CITY", null, skillResponses, List.of()
        );
    }

    private static JobResponse jobWithSkills(boolean remote, String location, Double salaryMin, List<JobResponse.JobSkillResponse> skills) {
        return new JobResponse(UUID.randomUUID(), "Java Developer", "CareerFlow", location, "FULL_TIME", salaryMin, null, "USD", remote, "Backend role", skills);
    }

    private static JobResponse.JobSkillResponse requiredSkill(String name) {
        return new JobResponse.JobSkillResponse(UUID.randomUUID(), name, true);
    }

    private static JobResponse.JobSkillResponse optionalSkill(String name) {
        return new JobResponse.JobSkillResponse(UUID.randomUUID(), name, false);
    }
}
