
package com.careerflow.profile.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateProfileRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestShouldHaveNoValidationErrors() {
        var request = new CreateCandidateProfileRequest(
                "Evgenii Buianov",
                "Java Backend Developer",
                "eug.java.dev@gmail.com",
                "+1 512 555 0100",
                "Austin, TX",
                "Java backend engineer.",
                List.of(new SkillRequest("Java", "Backend", new BigDecimal("6.0"))),
                List.of(new ExperienceRequest(
                        "Bank",
                        "Senior Java Developer",
                        "Austin, TX",
                        LocalDate.of(2021, 1, 1),
                        null,
                        true,
                        "Built microservices."
                ))
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void invalidRequestShouldReportTopLevelAndNestedValidationErrors() {
        var request = new CreateCandidateProfileRequest(
                "",
                "Java Backend Developer",
                "invalid-email",
                null,
                null,
                null,
                List.of(new SkillRequest("", "Backend", null)),
                List.of(new ExperienceRequest("", "", null, null, null, false, null))
        );

        var violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "fullName",
                        "email",
                        "skills[0].name",
                        "experiences[0].companyName",
                        "experiences[0].positionTitle"
                );
    }
}
