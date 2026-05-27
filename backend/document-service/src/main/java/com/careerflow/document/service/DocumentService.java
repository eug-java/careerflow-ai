/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.document.service;

import com.careerflow.document.dto.DocumentResponse;
import com.careerflow.document.dto.SaveGeneratedDocumentRequest;
import com.careerflow.document.entity.GeneratedDocument;
import com.careerflow.document.repository.GeneratedDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final GeneratedDocumentRepository repository;
    private final DocumentStorageService storageService;
    private final PdfExportService pdfExportService;
    private final DocxExportService docxExportService;

    public DocumentService(
            GeneratedDocumentRepository repository,
            DocumentStorageService storageService,
            PdfExportService pdfExportService,
            DocxExportService docxExportService
    ) {
        this.repository = repository;
        this.storageService = storageService;
        this.pdfExportService = pdfExportService;
        this.docxExportService = docxExportService;
    }

    @Transactional
    public DocumentResponse save(SaveGeneratedDocumentRequest request) {
        String normalizedType = request.documentType().trim().toUpperCase();

        String fileName = normalizedType.toLowerCase() + "-" + UUID.randomUUID() + ".md";
        String storageKey = "profiles/%s/jobs/%s/%s".formatted(
                request.profileId(),
                request.jobId(),
                fileName
        );

        String bucket = storageService.uploadText(storageKey, request.content());

        GeneratedDocument document = new GeneratedDocument();
        document.setProfileId(request.profileId());
        document.setJobId(request.jobId());
        document.setDocumentType(normalizedType);
        document.setFileName(fileName);
        document.setContentType("text/markdown");
        document.setStorageBucket(bucket);
        document.setStorageKey(storageKey);

        return toResponse(repository.save(document));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findAll(UUID profileId, UUID jobId) {
        if (profileId != null) {
            return repository.findByProfileId(profileId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        if (jobId != null) {
            return repository.findByJobId(jobId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(UUID id) {
        GeneratedDocument document = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));

        return toResponse(document);
    }

    private DocumentResponse toResponse(GeneratedDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getProfileId(),
                document.getJobId(),
                document.getDocumentType(),
                document.getFileName(),
                document.getContentType(),
                document.getStorageBucket(),
                document.getStorageKey(),
                document.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public String getContent(UUID id) {
        GeneratedDocument document = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));

        return storageService.downloadText(document.getStorageKey());
    }

    @Transactional
    public void delete(UUID id) {
        GeneratedDocument document = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));

        storageService.delete(document.getStorageKey());

        repository.delete(document);
    }

    @Transactional(readOnly = true)
    public byte[] downloadPdf(UUID id) {
        String content = getContent(id);
        return pdfExportService.toPdf(content);
    }

    @Transactional(readOnly = true)
    public byte[] downloadDocx(UUID id) {
        String content = getContent(id);
        return docxExportService.toDocx(content);
    }
}
