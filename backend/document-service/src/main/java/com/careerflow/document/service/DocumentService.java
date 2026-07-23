package com.careerflow.document.service;

import com.careerflow.common.api.ForbiddenException;
import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.event.DocumentGeneratedEvent;
import com.careerflow.common.security.CurrentUserProvider;
import com.careerflow.document.dto.DocumentResponse;
import com.careerflow.document.dto.SaveGeneratedDocumentRequest;
import com.careerflow.document.entity.GeneratedDocument;
import com.careerflow.document.entity.ProcessedEvent;
import com.careerflow.document.repository.GeneratedDocumentRepository;
import com.careerflow.document.repository.ProcessedEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final GeneratedDocumentRepository repository;
    private final ProcessedEventRepository processedEventRepository;
    private final DocumentStorageService storageService;
    private final PdfExportService pdfExportService;
    private final DocxExportService docxExportService;

    public DocumentService(
            GeneratedDocumentRepository repository,
            ProcessedEventRepository processedEventRepository,
            DocumentStorageService storageService,
            PdfExportService pdfExportService,
            DocxExportService docxExportService
    ) {
        this.repository = repository;
        this.processedEventRepository = processedEventRepository;
        this.storageService = storageService;
        this.pdfExportService = pdfExportService;
        this.docxExportService = docxExportService;
    }

    @Transactional
    public DocumentResponse save(SaveGeneratedDocumentRequest request) {
        return saveInternal(null, CurrentUserProvider.requireUserId(), request);
    }

    @Transactional
    public void saveFromEvent(DocumentGeneratedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }
        processedEventRepository.save(new ProcessedEvent(event.eventId()));
        SaveGeneratedDocumentRequest request = new SaveGeneratedDocumentRequest(
                event.profileId(),
                event.jobId(),
                event.documentType(),
                event.content()
        );
        try {
            saveInternal(event.eventId(), event.ownerId(), request);
        } catch (DataIntegrityViolationException ex) {
            // Duplicate source_event_id — safe to ignore
        }
    }

    private DocumentResponse saveInternal(UUID sourceEventId, UUID ownerId, SaveGeneratedDocumentRequest request) {
        String normalizedType = request.documentType().trim().toUpperCase();
        String fileName = normalizedType.toLowerCase() + "-" + UUID.randomUUID() + ".md";
        String storageKey = "profiles/%s/jobs/%s/%s".formatted(
                request.profileId(),
                request.jobId(),
                fileName
        );

        String bucket = storageService.uploadText(storageKey, request.content());

        GeneratedDocument document = new GeneratedDocument();
        document.setOwnerId(ownerId);
        document.setSourceEventId(sourceEventId);
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
        UUID ownerId = CurrentUserProvider.requireUserId();
        if (profileId != null) {
            return repository.findByOwnerIdAndProfileId(ownerId, profileId).stream().map(this::toResponse).toList();
        }
        if (jobId != null) {
            return repository.findByOwnerIdAndJobId(ownerId, jobId).stream().map(this::toResponse).toList();
        }
        return repository.findByOwnerId(ownerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(UUID id) {
        GeneratedDocument document = requireOwnedDocument(id);
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
        GeneratedDocument document = requireOwnedDocument(id);
        return storageService.downloadText(document.getStorageKey());
    }

    @Transactional
    public void delete(UUID id) {
        GeneratedDocument document = requireOwnedDocument(id);
        storageService.delete(document.getStorageKey());
        repository.delete(document);
    }

    @Transactional(readOnly = true)
    public byte[] downloadPdf(UUID id) {
        return pdfExportService.toPdf(getContent(id));
    }

    @Transactional(readOnly = true)
    public byte[] downloadDocx(UUID id) {
        return docxExportService.toDocx(getContent(id));
    }

    private GeneratedDocument requireOwnedDocument(UUID id) {
        GeneratedDocument document = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
        UUID ownerId = CurrentUserProvider.requireUserId();
        if (document.getOwnerId() != null && !ownerId.equals(document.getOwnerId())) {
            throw new ForbiddenException("Document access denied");
        }
        return document;
    }
}
