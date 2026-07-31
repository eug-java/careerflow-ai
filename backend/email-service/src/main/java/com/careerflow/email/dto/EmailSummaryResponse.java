package com.careerflow.email.dto;

import java.util.Map;

public record EmailSummaryResponse(
        long totalMessages,
        Map<EmailCategory, Long> byCategory,
        boolean accountConfigured
) {
}
