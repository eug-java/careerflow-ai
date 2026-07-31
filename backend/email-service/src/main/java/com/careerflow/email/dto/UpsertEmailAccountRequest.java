package com.careerflow.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpsertEmailAccountRequest(
        @NotBlank @Email String emailAddress,
        @NotBlank String password,
        @NotBlank String imapHost,
        @NotNull @Min(1) @Max(65535) Integer imapPort,
        @NotBlank String smtpHost,
        @NotNull @Min(1) @Max(65535) Integer smtpPort,
        @NotNull Boolean useSsl
) {
}
