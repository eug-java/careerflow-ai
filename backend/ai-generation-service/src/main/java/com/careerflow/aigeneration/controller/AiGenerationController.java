/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.controller;

import com.careerflow.aigeneration.dto.*;
import com.careerflow.aigeneration.service.AiGenerationService;
import com.careerflow.aigeneration.service.JobDescriptionParserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/generations")
public class AiGenerationController {

    private final AiGenerationService service;
    private final JobDescriptionParserService jobDescriptionParserService;

    public AiGenerationController(AiGenerationService service,
                                  JobDescriptionParserService jobDescriptionParserService) {
        this.service = service;
        this.jobDescriptionParserService = jobDescriptionParserService;
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

    @PostMapping("/content")
    public GenerateContentResponse generateContent(
            @Valid @RequestBody GenerateDocumentRequest request
    ) {
        return service.generateContentOnly(request);
    }
}
