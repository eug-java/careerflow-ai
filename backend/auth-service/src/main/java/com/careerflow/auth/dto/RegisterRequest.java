package com.careerflow.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may contain letters, numbers, and underscores only")
        String username,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}
