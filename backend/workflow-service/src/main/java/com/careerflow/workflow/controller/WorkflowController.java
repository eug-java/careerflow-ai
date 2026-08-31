package com.careerflow.workflow.controller;

import com.careerflow.common.security.CurrentUserProvider;
import com.careerflow.workflow.client.JobClient;
import com.careerflow.workflow.client.ProfileClient;
import com.careerflow.workflow.dto.StartBatchDocumentGenerationRequest;
import com.careerflow.workflow.dto.StartBatchWorkflowResponse;
import com.careerflow.workflow.dto.StartDocumentGenerationWorkflowRequest;
import com.careerflow.workflow.dto.StartWorkflowResponse;
import com.careerflow.workflow.dto.WorkflowListItem;
import com.careerflow.workflow.dto.WorkflowStatus;
import com.careerflow.workflow.service.WorkflowStatusService;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {
    private static final String DOCUMENT_GENERATION_PROCESS_ID = "document-generation-process";
    private final ZeebeClient zeebeClient;
    private final WorkflowStatusService workflowStatusService;
    private final ProfileClient profileClient;
    private final JobClient jobClient;

    public WorkflowController(
            ZeebeClient zeebeClient,
            WorkflowStatusService workflowStatusService,
            ProfileClient profileClient,
            JobClient jobClient
    ) {
        this.zeebeClient = zeebeClient;
        this.workflowStatusService = workflowStatusService;
        this.profileClient = profileClient;
        this.jobClient = jobClient;
    }

    @PostMapping("/document-generation")
    public StartWorkflowResponse startDocumentGeneration(@Valid @RequestBody StartDocumentGenerationWorkflowRequest request) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        return startSingleDocumentGeneration(ownerId, request);
    }

    @PostMapping("/document-generation/batch")
    public StartBatchWorkflowResponse startBatchDocumentGeneration(
            @Valid @RequestBody StartBatchDocumentGenerationRequest request
    ) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        List<StartWorkflowResponse> workflows = request.jobIds().stream()
                .map(jobId -> startSingleDocumentGeneration(
                        ownerId,
                        new StartDocumentGenerationWorkflowRequest(request.profileId(), jobId, request.documentType())
                ))
                .toList();
        return new StartBatchWorkflowResponse(workflows);
    }

    private StartWorkflowResponse startSingleDocumentGeneration(
            UUID ownerId,
            StartDocumentGenerationWorkflowRequest request
    ) {
        assertResourcesAccessible(request);

        ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(DOCUMENT_GENERATION_PROCESS_ID)
                .latestVersion()
                .variables(Map.of(
                        "profileId", request.profileId().toString(),
                        "jobId", request.jobId().toString(),
                        "documentType", request.documentType(),
                        "ownerId", ownerId.toString()
                ))
                .send()
                .join();

        workflowStatusService.markStarted(event.getProcessInstanceKey(), DOCUMENT_GENERATION_PROCESS_ID, ownerId);
        return new StartWorkflowResponse(event.getProcessInstanceKey(), event.getBpmnProcessId());
    }

    @GetMapping
    public List<WorkflowListItem> listWorkflows(@RequestParam(required = false) String status) {
        return workflowStatusService.listForCurrentUser(status);
    }

    @GetMapping("/{processInstanceKey}/status")
    public WorkflowStatus getStatus(@PathVariable long processInstanceKey) {
        return workflowStatusService.getStatus(processInstanceKey);
    }

    private void assertResourcesAccessible(StartDocumentGenerationWorkflowRequest request) {
        try {
            profileClient.assertAccessible(request.profileId());
            jobClient.assertAccessible(request.jobId());
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Profile or job not found or not accessible"
            );
        }
    }
}
