package com.careerflow.workflow.client;

import com.careerflow.common.client.InternalClientHeaders;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class AiGenerationClient {
    private final RestClient restClient;

    public AiGenerationClient(
            RestClient.Builder builder,
            @Value("${careerflow.services.ai-generation-url}") String aiGenerationServiceUrl,
            InternalClientHeaders internalClientHeaders
    ) {
        this.restClient = builder
                .baseUrl(aiGenerationServiceUrl)
                .defaultHeader(InternalClientHeaders.HEADER, internalClientHeaders.apiKey())
                .build();
    }

    @Retry(name = "aiGenerationServiceRetry")
    @CircuitBreaker(name = "aiGenerationServiceCircuitBreaker", fallbackMethod = "generateContentFallback")
    public GenerateContentResponse generateContent(UUID profileId, UUID jobId, String documentType) {
        return restClient.post()
                .uri("/api/v1/generations/content")
                .body(new GenerateContentRequest(profileId, jobId, documentType))
                .retrieve()
                .body(GenerateContentResponse.class);
    }

    private GenerateContentResponse generateContentFallback(UUID profileId, UUID jobId, String documentType, Throwable throwable) {
        String content = """
                # Generated Document

                AI generation service is temporarily unavailable.

                Profile ID: %s
                Job ID: %s
                Document Type: %s

                Please try again later.
                """.formatted(profileId, jobId, documentType);
        return new GenerateContentResponse(profileId, jobId, documentType, "FALLBACK", "workflow-service-fallback", content);
    }
}
