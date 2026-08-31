package com.careerflow.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GitHubOAuthRequest(
        @NotBlank String code
) {
}
