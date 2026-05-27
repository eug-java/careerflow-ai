package com.careerflow.aigeneration.controller;

import com.careerflow.aigeneration.dto.DocumentType;
import com.careerflow.aigeneration.dto.GenerateContentResponse;
import com.careerflow.aigeneration.dto.GenerateDocumentRequest;
import com.careerflow.aigeneration.dto.GenerateDocumentResponse;
import com.careerflow.aigeneration.dto.ParsedJobDescriptionResponse;
import com.careerflow.aigeneration.dto.ParsedJobSkillResponse;
import com.careerflow.aigeneration.service.AiGenerationService;
import com.careerflow.aigeneration.service.JobDescriptionParserService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiGenerationControllerTest {

    private static final UUID PROFILE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID JOB_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final AiGenerationService service = mock(AiGenerationService.class);
    private final JobDescriptionParserService parserService = mock(JobDescriptionParserService.class);
    private final AiGenerationController controller = new AiGenerationController(service, parserService);

    @Test
    void generateShouldDelegateToService() {
        GenerateDocumentRequest request = new GenerateDocumentRequest(PROFILE_ID, JOB_ID, DocumentType.RESUME);
        GenerateDocumentResponse expected = new GenerateDocumentResponse(
                PROFILE_ID,
                JOB_ID,
                DocumentType.RESUME,
                "AI",
                "gpt-4o-mini",
                "content",
                null,
                Instant.parse("2026-05-23T10:00:00Z")
        );
        when(service.generate(request)).thenReturn(expected);

        GenerateDocumentResponse actual = controller.generate(request);

        assertThat(actual).isEqualTo(expected);
        verify(service).generate(request);
    }

    @Test
    void generateContentShouldDelegateToService() {
        GenerateDocumentRequest request = new GenerateDocumentRequest(PROFILE_ID, JOB_ID, DocumentType.COVER_LETTER);
        GenerateContentResponse expected = new GenerateContentResponse(
                PROFILE_ID,
                JOB_ID,
                DocumentType.COVER_LETTER,
                "FALLBACK",
                "deterministic-template",
                "content"
        );
        when(service.generateContentOnly(request)).thenReturn(expected);

        GenerateContentResponse actual = controller.generateContent(request);

        assertThat(actual).isEqualTo(expected);
        verify(service).generateContentOnly(request);
    }

    @Test
    void parseJobDescriptionShouldDelegateToParserService() {
        ParsedJobDescriptionResponse expected = new ParsedJobDescriptionResponse(
                "Senior Java Engineer",
                "CareerFlow",
                "Austin, TX",
                "Full-time",
                120000.0,
                150000.0,
                "USD",
                true,
                "Build backend services.",
                List.of(new ParsedJobSkillResponse("Java", true))
        );
        when(parserService.parse("raw job description")).thenReturn(expected);

        ParsedJobDescriptionResponse actual = controller.parseJobDescription(
                new com.careerflow.aigeneration.dto.ParseJobDescriptionRequest("raw job description")
        );

        assertThat(actual).isEqualTo(expected);
        verify(parserService).parse("raw job description");
    }
}
