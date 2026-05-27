package com.careerflow.matching.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobMatchResultTest {

    @Test
    void prePersistAssignsIdAndCreatedAt() {
        JobMatchResult result = new JobMatchResult();

        result.prePersist();

        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void gettersAndSettersStoreValues() {
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        JobMatchResult result = new JobMatchResult();

        result.setProfileId(profileId);
        result.setJobId(jobId);
        result.setTotalScore(new BigDecimal("95.00"));
        result.setSkillsScore(new BigDecimal("90.00"));
        result.setLocationScore(new BigDecimal("100"));
        result.setSalaryScore(new BigDecimal("75.00"));
        result.setExplanation("Good match");

        assertThat(result.getProfileId()).isEqualTo(profileId);
        assertThat(result.getJobId()).isEqualTo(jobId);
        assertThat(result.getTotalScore()).isEqualByComparingTo("95.00");
        assertThat(result.getSkillsScore()).isEqualByComparingTo("90.00");
        assertThat(result.getLocationScore()).isEqualByComparingTo("100");
        assertThat(result.getSalaryScore()).isEqualByComparingTo("75.00");
        assertThat(result.getExplanation()).isEqualTo("Good match");
    }
}
