package com.careerflow.email.dto;

import java.time.Instant;
import java.util.UUID;

public record InboxMessageResponse(
        UUID id,
        String subject,
        String fromAddress,
        String toAddress,
        String bodyPreview,
        String bodyText,
        Instant receivedAt,
        EmailCategory category,
        String classificationReason,
        Instant repliedAt,
        boolean replied
) {
}
