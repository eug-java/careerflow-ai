package com.careerflow.email.dto;

import java.time.Instant;

public record EmailAccountResponse(
        String emailAddress,
        String imapHost,
        int imapPort,
        String smtpHost,
        int smtpPort,
        boolean useSsl,
        Instant updatedAt,
        boolean configured
) {
}
