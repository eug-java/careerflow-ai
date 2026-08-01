package com.careerflow.aigeneration.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertAiAccountRequest(
        @NotBlank String apiKey,
        String provider,
        String preferredModel
) {
}
