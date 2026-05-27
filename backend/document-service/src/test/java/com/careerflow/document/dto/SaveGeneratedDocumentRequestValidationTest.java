package com.careerflow.document.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SaveGeneratedDocumentRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestShouldHaveNoValidationErrors() {
        SaveGeneratedDocumentRequest request = new SaveGeneratedDocumentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESUME",
                "content"
        );

        Set<ConstraintViolation<SaveGeneratedDocumentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void invalidRequestShouldRequireIdsTypeAndContent() {
        SaveGeneratedDocumentRequest request = new SaveGeneratedDocumentRequest(
                null,
                null,
                " ",
                ""
        );

        Set<String> invalidFields = validator.validate(request)
                .stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(invalidFields).containsExactlyInAnyOrder("profileId", "jobId", "documentType", "content");
    }
}
