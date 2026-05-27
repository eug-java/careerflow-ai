/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.client.JobClient;
import com.careerflow.aigeneration.client.JobResponse;
import com.careerflow.aigeneration.client.ProfileClient;
import com.careerflow.aigeneration.client.ProfileResponse;
import com.careerflow.aigeneration.dto.GenerateContentResponse;
import com.careerflow.aigeneration.dto.GenerateDocumentRequest;
import com.careerflow.aigeneration.dto.GenerateDocumentResponse;
import com.careerflow.aigeneration.event.DocumentGeneratedEvent;
import com.careerflow.aigeneration.event.DocumentGeneratedEventPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.UUID;

@Service
public class AiGenerationService {

    private final ProfileClient profileClient;
    private final JobClient jobClient;
    private final DocumentGeneratedEventPublisher eventPublisher;
    private final AiResumeGenerator aiResumeGenerator;
    private final DraftContentGenerator fallbackGenerator;
    private static final Logger log = LoggerFactory.getLogger(AiGenerationService.class);
    private final Counter generationCounter;
    private final AiMetricsService aiMetricsService;


    private  MeterRegistry meterRegistry;
    public void recordRequest(String documentType) {
        Counter.builder("ai_generation_requests_total")
                .description("Total AI generation requests")
                .tag("document_type", documentType)
                .register(meterRegistry);
    }

    public AiGenerationService(
            ProfileClient profileClient,
            JobClient jobClient,
            DraftContentGenerator fallbackGenerator,
            AiResumeGenerator aiResumeGenerator,
            DocumentGeneratedEventPublisher eventPublisher,
            MeterRegistry meterRegistry,
            AiMetricsService aiMetricsService
    ) {
        this.profileClient = profileClient;
        this.jobClient = jobClient;
        this.fallbackGenerator = fallbackGenerator;
        this.aiResumeGenerator = aiResumeGenerator;
        this.eventPublisher = eventPublisher;
        this.generationCounter = Counter.builder("careerflow_ai_generations_total")
                .description("Total generated AI documents")
                .register(meterRegistry);
        this.aiMetricsService = aiMetricsService;
    }
    public GenerateDocumentResponse generate(GenerateDocumentRequest request) {
        aiMetricsService.recordRequest(request.documentType().name());
        ProfileResponse profile;
        try {
             profile = profileClient.getProfile(request.profileId());
        } catch (WebClientResponseException.NotFound ex) {
            throw new IllegalArgumentException(
                    "Profile not found: " + request.profileId()
            );
        }
        JobResponse job = jobClient.getJob(request.jobId());

        String content;
        String generationMode;
        String model = "gpt-4o-mini";

        try {
            content = aiResumeGenerator.generate(profile, job, request.documentType());
            generationMode = "AI";
            generationCounter.increment();
            aiMetricsService.recordSuccess(
                    request.documentType().name(),
                    generationMode,
                    model
            );
        } catch (Exception e) {
            log.warn("AI generation failed. Falling back to deterministic template", e);
            aiMetricsService.recordFallback(
                    request.documentType().name(),
                    e.getClass().getSimpleName()
            );
            content = fallbackGenerator.generate(profile, job, request.documentType());
            generationMode = "FALLBACK";
            model = "deterministic-template";
        }
        Instant generatedAt = Instant.now();

        eventPublisher.publish(
                new DocumentGeneratedEvent(
                        UUID.randomUUID(),
                        request.profileId(),
                        request.jobId(),
                        request.documentType().name(),
                        content,
                        generatedAt
                )
        );

        return new GenerateDocumentResponse(
                request.profileId(),
                request.jobId(),
                request.documentType(),
                generationMode,
                model,
                content,
                null,
                generatedAt
        );
    }

    public GenerateContentResponse generateContentOnly(GenerateDocumentRequest request) {
        aiMetricsService.recordRequest(request.documentType().name());
        ProfileResponse profile = profileClient.getProfile(request.profileId());
        JobResponse job = jobClient.getJob(request.jobId());

        String content;
        String generationMode;
        String model = "gpt-4o-mini";

        try {
            content = aiResumeGenerator.generate(profile, job, request.documentType());
            generationMode = "AI";
            aiMetricsService.recordSuccess(
                    request.documentType().name(),
                    generationMode,
                    model
            );
        } catch (Exception e) {
            log.warn("AI generation failed. Falling back to deterministic template", e);
            aiMetricsService.recordFallback(
                    request.documentType().name(),
                    e.getClass().getSimpleName()
            );
            content = fallbackGenerator.generate(profile, job, request.documentType());
            generationMode = "FALLBACK";
            model = "deterministic-template";
        }

        return new GenerateContentResponse(
                request.profileId(),
                request.jobId(),
                request.documentType(),
                generationMode,
                model,
                content
        );
    }
}
