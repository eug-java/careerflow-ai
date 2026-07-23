package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.client.JobClient;
import com.careerflow.aigeneration.client.ProfileClient;
import com.careerflow.aigeneration.dto.DocumentType;
import com.careerflow.aigeneration.dto.GenerateContentResponse;
import com.careerflow.aigeneration.dto.GenerateDocumentRequest;
import com.careerflow.aigeneration.dto.GenerateDocumentResponse;
import com.careerflow.common.event.DocumentGeneratedEvent;
import com.careerflow.common.test.TestAuthSupport;
import com.careerflow.aigeneration.event.DocumentGeneratedEventPublisher;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiGenerationServiceTest {

    private ProfileClient profileClient;
    private JobClient jobClient;
    private DraftContentGenerator fallbackGenerator;
    private AiResumeGenerator aiResumeGenerator;
    private DocumentGeneratedEventPublisher eventPublisher;
    private SimpleMeterRegistry meterRegistry;
    private AiGenerationService service;
    private AiMetricsService aiMetricsService;

    @BeforeEach
    void setUp() {
        profileClient = mock(ProfileClient.class);
        jobClient = mock(JobClient.class);
        fallbackGenerator = mock(DraftContentGenerator.class);
        aiResumeGenerator = mock(AiResumeGenerator.class);
        eventPublisher = mock(DocumentGeneratedEventPublisher.class);
        meterRegistry = new SimpleMeterRegistry();
        aiMetricsService = mock(AiMetricsService.class);

        service = new AiGenerationService(
                profileClient,
                jobClient,
                fallbackGenerator,
                aiResumeGenerator,
                eventPublisher,
                meterRegistry,
                aiMetricsService
        );
        TestAuthSupport.authenticateTestUser();
    }

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
    }

    @Test
    void generateShouldUseAiContentAndPublishEvent() {
        GenerateDocumentRequest request = new GenerateDocumentRequest(
                TestData.PROFILE_ID,
                TestData.JOB_ID,
                DocumentType.RESUME
        );
        var profile = TestData.profile();
        var job = TestData.job();
        when(profileClient.getProfile(TestData.PROFILE_ID)).thenReturn(profile);
        when(jobClient.getJob(TestData.JOB_ID)).thenReturn(job);
        when(aiResumeGenerator.generate(profile, job, DocumentType.RESUME))
                .thenReturn("AI resume content");

        GenerateDocumentResponse response = service.generate(request);

        assertThat(response.profileId()).isEqualTo(TestData.PROFILE_ID);
        assertThat(response.jobId()).isEqualTo(TestData.JOB_ID);
        assertThat(response.documentType()).isEqualTo(DocumentType.RESUME);
        assertThat(response.generationMode()).isEqualTo("AI");
        assertThat(response.model()).isEqualTo("gpt-4o-mini");
        assertThat(response.content()).isEqualTo("AI resume content");
        assertThat(response.generatedAt()).isNotNull();
        assertThat(response.savedDocument()).isNull();

        ArgumentCaptor<DocumentGeneratedEvent> eventCaptor = ArgumentCaptor.forClass(DocumentGeneratedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        DocumentGeneratedEvent event = eventCaptor.getValue();
        assertThat(event.eventId()).isNotNull();
        assertThat(event.profileId()).isEqualTo(TestData.PROFILE_ID);
        assertThat(event.jobId()).isEqualTo(TestData.JOB_ID);
        assertThat(event.documentType()).isEqualTo("RESUME");
        assertThat(event.content()).isEqualTo("AI resume content");
        assertThat(event.generatedAt()).isNotNull();

        assertThat(meterRegistry.counter("careerflow_ai_generations_total").count()).isEqualTo(1.0);
        verifyNoInteractions(fallbackGenerator);
    }

    @Test
    void generateShouldFallbackWhenAiGenerationFails() {
        GenerateDocumentRequest request = new GenerateDocumentRequest(
                TestData.PROFILE_ID,
                TestData.JOB_ID,
                DocumentType.COVER_LETTER
        );
        var profile = TestData.profile();
        var job = TestData.job();
        when(profileClient.getProfile(TestData.PROFILE_ID)).thenReturn(profile);
        when(jobClient.getJob(TestData.JOB_ID)).thenReturn(job);
        when(aiResumeGenerator.generate(any(), any(), eq(DocumentType.COVER_LETTER)))
                .thenThrow(new RuntimeException("OpenAI is unavailable"));
        when(fallbackGenerator.generate(profile, job, DocumentType.COVER_LETTER))
                .thenReturn("Fallback cover letter");

        GenerateDocumentResponse response = service.generate(request);

        assertThat(response.generationMode()).isEqualTo("FALLBACK");
        assertThat(response.model()).isEqualTo("deterministic-template");
        assertThat(response.content()).isEqualTo("Fallback cover letter");
        assertThat(meterRegistry.counter("careerflow_ai_generations_total").count()).isEqualTo(0.0);
        verify(eventPublisher).publish(any(DocumentGeneratedEvent.class));
    }

    @Test
    void generateShouldThrowIllegalArgumentExceptionWhenProfileNotFound() {
        GenerateDocumentRequest request = new GenerateDocumentRequest(
                TestData.PROFILE_ID,
                TestData.JOB_ID,
                DocumentType.RESUME
        );
        WebClientResponseException notFound = WebClientResponseException.create(
                404,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8
        );
        when(profileClient.getProfile(TestData.PROFILE_ID)).thenThrow(notFound);

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Profile not found")
                .hasMessageContaining(TestData.PROFILE_ID.toString());

        verifyNoInteractions(jobClient, aiResumeGenerator, fallbackGenerator, eventPublisher);
    }

    @Test
    void generateContentOnlyShouldNotPublishEvent() {
        GenerateDocumentRequest request = new GenerateDocumentRequest(
                TestData.PROFILE_ID,
                TestData.JOB_ID,
                DocumentType.RESUME
        );
        var profile = TestData.profile();
        var job = TestData.job();
        when(profileClient.getProfile(TestData.PROFILE_ID)).thenReturn(profile);
        when(jobClient.getJob(TestData.JOB_ID)).thenReturn(job);
        when(aiResumeGenerator.generate(profile, job, DocumentType.RESUME))
                .thenReturn("AI content only");

        GenerateContentResponse response = service.generateContentOnly(request);

        assertThat(response.profileId()).isEqualTo(TestData.PROFILE_ID);
        assertThat(response.jobId()).isEqualTo(TestData.JOB_ID);
        assertThat(response.documentType()).isEqualTo(DocumentType.RESUME);
        assertThat(response.generationMode()).isEqualTo("AI");
        assertThat(response.model()).isEqualTo("gpt-4o-mini");
        assertThat(response.content()).isEqualTo("AI content only");
        verifyNoInteractions(eventPublisher);
    }
}
