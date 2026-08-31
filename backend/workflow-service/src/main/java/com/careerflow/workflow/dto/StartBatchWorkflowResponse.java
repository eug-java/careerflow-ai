package com.careerflow.workflow.dto;

import java.util.List;

public record StartBatchWorkflowResponse(
        List<StartWorkflowResponse> workflows
) {
}
