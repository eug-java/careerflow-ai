package com.careerflow.workflow.dto;

import java.time.Instant;

public record WorkflowListItem(
        long processInstanceKey,
        String processId,
        String status,
        String message,
        Instant updatedAt
) {
}
