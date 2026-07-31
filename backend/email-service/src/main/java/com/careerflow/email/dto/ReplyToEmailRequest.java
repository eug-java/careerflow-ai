package com.careerflow.email.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record ReplyToEmailRequest(
        @NotEmpty List<UUID> documentIds,
        @NotBlank String bodyText
) {
}
