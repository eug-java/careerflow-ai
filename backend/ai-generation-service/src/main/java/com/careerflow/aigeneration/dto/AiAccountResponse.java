package com.careerflow.aigeneration.dto;

import java.time.Instant;

public record AiAccountResponse(
        String provider,
        String preferredModel,
        String apiKeyHint,
        Instant updatedAt,
        boolean configured
) {
}
