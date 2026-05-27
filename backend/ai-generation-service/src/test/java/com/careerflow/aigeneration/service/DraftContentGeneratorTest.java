package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.dto.DocumentType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DraftContentGeneratorTest {

    private final DraftContentGenerator generator = new DraftContentGenerator();

    @Test
    void generateResumeShouldIncludeCandidateJobSkillsAndExperience() {
        String content = generator.generate(TestData.profile(), TestData.job(), DocumentType.RESUME);

        assertThat(content)
                .contains("Evgenii Buianov")
                .contains("Java Backend Developer")
                .contains("Senior Java Engineer")
                .contains("CareerFlow")
                .contains("Java, Spring Boot, Kafka")
                .contains("Senior Java Developer, Bank Project")
                .contains("Built Java microservices with Spring Boot and Kafka.");
    }

    @Test
    void generateCoverLetterShouldIncludeApplicantAndTargetCompany() {
        String content = generator.generate(TestData.profile(), TestData.job(), DocumentType.COVER_LETTER);

        assertThat(content)
                .contains("Dear Hiring Team")
                .contains("Senior Java Engineer")
                .contains("CareerFlow")
                .contains("Java Backend Developer")
                .contains("Evgenii Buianov");
    }

    @Test
    void generateShouldHandleNullSkillsAndExperience() {
        String content = generator.generate(
                TestData.profileWithoutSkillsAndExperience(),
                TestData.job(),
                DocumentType.RESUME
        );

        assertThat(content)
                .contains("Evgenii Buianov")
                .contains("Professional Summary")
                .contains("Relevant Skills")
                .doesNotContain("null");
    }
}
