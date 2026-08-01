/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.controller;

import com.careerflow.aigeneration.dto.*;
import com.careerflow.aigeneration.service.AiGenerationService;
import com.careerflow.aigeneration.service.JobDescriptionParserService;
import com.careerflow.aigeneration.service.ResumeParserService;
import com.careerflow.aigeneration.service.ResumeTextExtractor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/generations")
public class AiGenerationController {

    private final AiGenerationService service;
    private final JobDescriptionParserService jobDescriptionParserService;
    private final ResumeParserService resumeParserService;
    private final ResumeTextExtractor resumeTextExtractor;

    public AiGenerationController(AiGenerationService service,
                                  JobDescriptionParserService jobDescriptionParserService,
                                  ResumeParserService resumeParserService,
                                  ResumeTextExtractor resumeTextExtractor) {
        this.service = service;
        this.jobDescriptionParserService = jobDescriptionParserService;
        this.resumeParserService = resumeParserService;
        this.resumeTextExtractor = resumeTextExtractor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GenerateDocumentResponse generate(@Valid @RequestBody GenerateDocumentRequest request) {
        return service.generate(request);
    }

    @PostMapping("/jobs/parse")
    public ParsedJobDescriptionResponse parseJobDescription(
            @Valid @RequestBody ParseJobDescriptionRequest request
    ) {
        return jobDescriptionParserService.parse(request.text());
    }

    @PostMapping("/profiles/parse")
    public ParsedResumeResponse parseResume(@Valid @RequestBody ParseResumeRequest request) {
        return resumeParserService.parse(request.text());
    }

    @PostMapping(value = "/profiles/parse-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ParsedResumeResponse parseResumeFile(@RequestParam("file") MultipartFile file) {
        return resumeParserService.parse(resumeTextExtractor.extractText(file));
    }

    @PostMapping("/content")
    public GenerateContentResponse generateContent(
            @Valid @RequestBody GenerateDocumentRequest request
    ) {
        return service.generateContentOnly(request);
    }
}
