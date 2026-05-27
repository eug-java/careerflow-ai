
package com.careerflow.profile.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalField;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateEntityLifecycleTest {

    @Test
    void candidateProfilePrePersistShouldSetIdAndTimestamps() {
        CandidateProfile profile = new CandidateProfile();
        profile.setFullName("Evgenii Buianov");

        profile.prePersist();

        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getCreatedAt()).isNotNull();
        assertThat(profile.getUpdatedAt()).isNotNull();
        assertThat((profile.getUpdatedAt().getEpochSecond() - profile.getCreatedAt().getEpochSecond()) < 2);
    }

    @Test
    void candidateProfilePreUpdateShouldRefreshUpdatedAtOnly() throws InterruptedException {
        CandidateProfile profile = new CandidateProfile();
        profile.prePersist();

        var createdAt = profile.getCreatedAt();
        var firstUpdatedAt = profile.getUpdatedAt();

        Thread.sleep(2);
        profile.preUpdate();

        assertThat(profile.getCreatedAt()).isEqualTo(createdAt);
        assertThat(profile.getUpdatedAt()).isAfter(firstUpdatedAt);
    }

    @Test
    void candidateSkillPrePersistShouldSetIdAndKeepFields() {
        CandidateProfile profile = new CandidateProfile();
        CandidateSkill skill = new CandidateSkill();
        skill.setProfile(profile);
        skill.setName("Java");
        skill.setCategory("Backend");
        skill.setYearsOfExperience(new BigDecimal("6.0"));

        skill.prePersist();

        assertThat(skill.getId()).isNotNull();
        assertThat(skill.getProfile()).isSameAs(profile);
        assertThat(skill.getName()).isEqualTo("Java");
        assertThat(skill.getCategory()).isEqualTo("Backend");
        assertThat(skill.getYearsOfExperience()).isEqualByComparingTo("6.0");
    }

    @Test
    void candidateExperiencePrePersistShouldSetIdAndKeepFields() {
        CandidateProfile profile = new CandidateProfile();
        CandidateExperience experience = new CandidateExperience();
        experience.setProfile(profile);
        experience.setCompanyName("Bank");
        experience.setPositionTitle("Senior Java Developer");
        experience.setLocation("Austin, TX");
        experience.setStartDate(LocalDate.of(2021, 1, 1));
        experience.setEndDate(LocalDate.of(2024, 1, 1));
        experience.setCurrentPosition(false);
        experience.setDescription("Built microservices.");

        experience.prePersist();

        assertThat(experience.getId()).isNotNull();
        assertThat(experience.getProfile()).isSameAs(profile);
        assertThat(experience.getCompanyName()).isEqualTo("Bank");
        assertThat(experience.getPositionTitle()).isEqualTo("Senior Java Developer");
        assertThat(experience.getLocation()).isEqualTo("Austin, TX");
        assertThat(experience.getStartDate()).isEqualTo(LocalDate.of(2021, 1, 1));
        assertThat(experience.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(experience.isCurrentPosition()).isFalse();
        assertThat(experience.getDescription()).isEqualTo("Built microservices.");
    }
}
