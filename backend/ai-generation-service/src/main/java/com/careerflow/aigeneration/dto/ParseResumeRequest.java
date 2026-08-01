package com.careerflow.aigeneration.dto;

import jakarta.validation.constraints.NotBlank;

public record ParseResumeRequest(
        @NotBlank String text
) {
}
