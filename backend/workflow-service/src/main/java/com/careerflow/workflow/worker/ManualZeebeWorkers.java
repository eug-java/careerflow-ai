/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.worker;

import com.careerflow.workflow.client.AiGenerationClient;
import com.careerflow.workflow.client.GenerateContentResponse;
import com.careerflow.common.event.DocumentGeneratedEvent;
import com.careerflow.workflow.event.DocumentGeneratedEventPublisher;
import com.careerflow.workflow.service.WorkflowStatusService;
import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class ManualZeebeWorkers {

    private static final Logger log = LoggerFactory.getLogger(ManualZeebeWorkers.class);

    private final ZeebeClient zeebeClient;
    private final DocumentGeneratedEventPublisher eventPublisher;
    private final WorkflowStatusService workflowStatusService;
    private final AiGenerationClient aiGenerationClient;

    public ManualZeebeWorkers(
            ZeebeClient zeebeClient,
            DocumentGeneratedEventPublisher eventPublisher,
            WorkflowStatusService workflowStatusService,
            AiGenerationClient aiGenerationClient
    ) {
        this.zeebeClient = zeebeClient;
        this.eventPublisher = eventPublisher;
        this.workflowStatusService = workflowStatusService;
        this.aiGenerationClient = aiGenerationClient;
    }

    @PostConstruct
    public void startWorkers() {
        zeebeClient.newWorker()
                .jobType("generate-document")
                .handler((client, job) -> {
                    try {
                        Map<String, Object> variables = job.getVariablesAsMap();

                        String profileId = (String) variables.get("profileId");
                        String jobId = (String) variables.get("jobId");
                        String documentType = (String) variables.get("documentType");

                        GenerateContentResponse aiResponse =
                                aiGenerationClient.generateContent(
                                        UUID.fromString(profileId),
                                        UUID.fromString(jobId),
                                        documentType
                                );

                        String content = aiResponse.content();

                        log.info(
                                "Generated document via AI service. profileId={}, jobId={}, documentType={}, mode={}, model={}, contentLength={}",
                                profileId,
                                jobId,
                                documentType,
                                aiResponse.generationMode(),
                                aiResponse.model(),
                                content == null ? 0 : content.length()
                        );

                        client.newCompleteCommand(job.getKey())
                                .variables(Map.of(
                                        "generatedContent", content,
                                        "generationMode", aiResponse.generationMode(),
                                        "model", aiResponse.model()
                                ))
                                .send()
                                .join();

                    } catch (Exception e) {
                        workflowStatusService.markFailed(
                                job.getProcessInstanceKey(),
                                e.getMessage()
                        );

                        log.error(
                                "Failed to generate document. processInstanceKey={}",
                                job.getProcessInstanceKey(),
                                e
                        );

                        throw e;
                    }
                })
                .name("generate-document-worker")
                .open();

        zeebeClient.newWorker()
                .jobType("publish-document-event")
                .handler((client, job) -> {
                    try {
                        Map<String, Object> variables = job.getVariablesAsMap();

                        String profileId = (String) variables.get("profileId");
                        String jobId = (String) variables.get("jobId");
                        String documentType = (String) variables.get("documentType");
                        String generatedContent = (String) variables.get("generatedContent");
                        String ownerId = (String) variables.get("ownerId");

                        eventPublisher.publish(
                                new DocumentGeneratedEvent(
                                        UUID.randomUUID(),
                                        UUID.fromString(ownerId),
                                        UUID.fromString(profileId),
                                        UUID.fromString(jobId),
                                        documentType,
                                        generatedContent,
                                        Instant.now()
                                )
                        );

                        log.info(
                                "Published document.generated event from workflow. profileId={}, jobId={}, documentType={}, contentLength={}",
                                profileId,
                                jobId,
                                documentType,
                                generatedContent == null ? 0 : generatedContent.length()
                        );

                        client.newCompleteCommand(job.getKey())
                                .send()
                                .join();

                        workflowStatusService.markCompleted(job.getProcessInstanceKey());

                    } catch (Exception e) {
                        workflowStatusService.markFailed(
                                job.getProcessInstanceKey(),
                                e.getMessage()
                        );

                        log.error(
                                "Failed to publish document event. processInstanceKey={}",
                                job.getProcessInstanceKey(),
                                e
                        );

                        throw e;
                    }
                })
                .name("publish-document-event-worker")
                .open();
    }
}
