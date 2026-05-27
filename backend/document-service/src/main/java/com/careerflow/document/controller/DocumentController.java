/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.document.controller;

import com.careerflow.document.dto.DocumentResponse;
import com.careerflow.document.dto.SaveGeneratedDocumentRequest;
import com.careerflow.document.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse save(@Valid @RequestBody SaveGeneratedDocumentRequest request) {
        return service.save(request);
    }

    @GetMapping
    public List<DocumentResponse> findAll(
            @RequestParam(required = false) UUID profileId,
            @RequestParam(required = false) UUID jobId
    ) {
        return service.findAll(profileId, jobId);
    }

    @GetMapping("/{id}")
    public DocumentResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping(value = "/{id}/content", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getContent(@PathVariable UUID id) {
        return service.getContent(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        byte[] pdf = service.downloadPdf(id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"document-%s.pdf\"".formatted(id)
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(
            value = "/{id}/docx",
            produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )
    public ResponseEntity<byte[]> downloadDocx(@PathVariable UUID id) {
        byte[] docx = service.downloadDocx(id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"document-%s.docx\"".formatted(id)
                )
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
                .body(docx);
    }
}
