package com.careerflow.workflow.controller;

import com.careerflow.workflow.dto.StartDocumentGenerationWorkflowRequest;
import com.careerflow.workflow.dto.StartWorkflowResponse;
import com.careerflow.workflow.dto.WorkflowStatus;
import com.careerflow.workflow.service.WorkflowStatusService;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.validation.Valid;
/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {
    private static final String DOCUMENT_GENERATION_PROCESS_ID = "document-generation-process";
    private final ZeebeClient zeebeClient;
    private final WorkflowStatusService workflowStatusService;

    public WorkflowController(ZeebeClient zeebeClient, WorkflowStatusService workflowStatusService) {
        this.zeebeClient = zeebeClient;
        this.workflowStatusService = workflowStatusService;
    }

    @PostMapping("/document-generation")
    public StartWorkflowResponse startDocumentGeneration(@Valid @RequestBody StartDocumentGenerationWorkflowRequest request) {
        ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(DOCUMENT_GENERATION_PROCESS_ID)
                .latestVersion()
                .variables(Map.of(
                        "profileId", request.profileId().toString(),
                        "jobId", request.jobId().toString(),
                        "documentType", request.documentType()))
                .send()
                .join();

        workflowStatusService.markStarted(event.getProcessInstanceKey(), DOCUMENT_GENERATION_PROCESS_ID);
        return new StartWorkflowResponse(event.getProcessInstanceKey(), event.getBpmnProcessId());
    }

    @GetMapping("/{processInstanceKey}/status")
    public WorkflowStatus getStatus(@PathVariable long processInstanceKey) {
        return workflowStatusService.getStatus(processInstanceKey);
    }
}
