package com.careerflow.matching.service;

import com.careerflow.matching.client.JobResponse;
import com.careerflow.matching.client.ProfileResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingScoreCalculatorTest {

    private final MatchingScoreCalculator calculator = new MatchingScoreCalculator();

    @Test
    void calculateReturnsPerfectScoreForAllRequiredSkillsRemoteJobAndSalaryProvided() {
        ProfileResponse profile = profileWithSkills("Java", "Spring Boot", "Kafka");
        JobResponse job = jobWithSkills(true, "Austin, TX", 120_000.0, List.of(
                requiredSkill("java"),
                requiredSkill("Spring Boot"),
                optionalSkill("React")
        ));

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.skillsScore()).isEqualByComparingTo("100.00");
        assertThat(score.locationScore()).isEqualByComparingTo("100");
        assertThat(score.salaryScore()).isEqualByComparingTo("100");
        assertThat(score.totalScore()).isEqualByComparingTo("100.00");
        assertThat(score.explanation()).contains("Skills score", "Total score formula");
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

        assertThat(score.skillsScore()).isEqualByComparingTo("50.00");
        assertThat(score.locationScore()).isEqualByComparingTo("100");
        assertThat(score.salaryScore()).isEqualByComparingTo("50");
        assertThat(score.totalScore()).isEqualByComparingTo("60.00");
    }

    @Test
    void calculateReturnsZeroSkillsScoreWhenJobHasNoSkills() {
        ProfileResponse profile = profileWithSkills("Java");
        JobResponse job = jobWithSkills(false, "Dallas, TX", null, List.of());

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.skillsScore()).isEqualByComparingTo("0");
        assertThat(score.locationScore()).isEqualByComparingTo("70");
        assertThat(score.salaryScore()).isEqualByComparingTo("50");
        assertThat(score.totalScore()).isEqualByComparingTo("19.00");
    }

    @Test
    void calculateReturnsHundredSkillsScoreWhenNoRequiredSkillsExist() {
        ProfileResponse profile = profileWithSkills();
        JobResponse job = jobWithSkills(false, "New York, NY", null, List.of(optionalSkill("Java")));

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.skillsScore()).isEqualByComparingTo("100");
    }

    @Test
    void calculateReturnsFallbackLocationScoreWhenLocationIsMissing() {
        ProfileResponse profile = new ProfileResponse(UUID.randomUUID(), "Name", "Engineer", "a@b.com", null, null, null, List.of());
        JobResponse job = jobWithSkills(false, null, null, List.of(requiredSkill("Java")));

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.locationScore()).isEqualByComparingTo("50");
    }

    @Test
    void calculateReturnsLowLocationScoreForDifferentStates() {
        ProfileResponse profile = new ProfileResponse(UUID.randomUUID(), "Name", "Engineer", "a@b.com", null, "Austin, TX", null, List.of());
        JobResponse job = jobWithSkills(false, "Seattle, WA", null, List.of(requiredSkill("Java")));

        MatchingScoreCalculator.MatchScore score = calculator.calculate(profile, job);

        assertThat(score.locationScore()).isEqualByComparingTo("30");
    }

    private static ProfileResponse profileWithSkills(String... skills) {
        List<ProfileResponse.SkillResponse> skillResponses = java.util.Arrays.stream(skills)
                .map(skill -> new ProfileResponse.SkillResponse(UUID.randomUUID(), skill, "backend", BigDecimal.ONE))
                .toList();
        return new ProfileResponse(UUID.randomUUID(), "Jane Doe", "Java Developer", "jane@example.com", null, "Austin, TX", null, skillResponses);
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
