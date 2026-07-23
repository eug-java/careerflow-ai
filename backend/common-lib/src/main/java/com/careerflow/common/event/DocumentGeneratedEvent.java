package com.careerflow.common.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentGeneratedEvent(
        UUID eventId,
        UUID ownerId,
        UUID profileId,
        UUID jobId,
        String documentType,
        String content,
        Instant generatedAt
) {
}
