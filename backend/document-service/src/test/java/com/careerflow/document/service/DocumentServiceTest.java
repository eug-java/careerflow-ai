package com.careerflow.document.service;

import com.careerflow.common.api.ForbiddenException;
import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.test.TestAuthSupport;
import com.careerflow.document.dto.DocumentResponse;
import com.careerflow.common.event.DocumentGeneratedEvent;
import com.careerflow.document.dto.SaveGeneratedDocumentRequest;
import com.careerflow.document.entity.GeneratedDocument;
import com.careerflow.document.entity.ProcessedEvent;
import com.careerflow.document.repository.GeneratedDocumentRepository;
import com.careerflow.document.repository.ProcessedEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private GeneratedDocumentRepository repository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private DocumentStorageService storageService;

    @Mock
    private PdfExportService pdfExportService;

    @Mock
    private DocxExportService docxExportService;

    private DocumentService documentService;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(
                repository,
                processedEventRepository,
                storageService,
                pdfExportService,
                docxExportService
        );
        ownerId = TestAuthSupport.authenticateTestUser();
    }

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
    }

    @Test
    void saveShouldNormalizeTypeUploadContentAndPersistDocument() {
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        SaveGeneratedDocumentRequest request = new SaveGeneratedDocumentRequest(
                profileId,
                jobId,
                " resume ",
                "# Generated resume"
        );

        when(storageService.uploadText(any(String.class), any(String.class))).thenReturn("careerflow-documents");
        when(repository.save(any(GeneratedDocument.class))).thenAnswer(invocation -> {
            GeneratedDocument document = invocation.getArgument(0);
            ReflectionTestUtils.setField(document, "id", documentId);
            ReflectionTestUtils.setField(document, "createdAt", Instant.parse("2026-05-23T10:00:00Z"));
            return document;
        });

        DocumentResponse response = documentService.save(request);

        ArgumentCaptor<String> storageKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageService).uploadText(storageKeyCaptor.capture(), eq("# Generated resume"));
        assertThat(storageKeyCaptor.getValue())
                .startsWith("profiles/%s/jobs/%s/resume-".formatted(profileId, jobId))
                .endsWith(".md");

        ArgumentCaptor<GeneratedDocument> documentCaptor = ArgumentCaptor.forClass(GeneratedDocument.class);
        verify(repository).save(documentCaptor.capture());
        GeneratedDocument savedDocument = documentCaptor.getValue();

        assertThat(savedDocument.getOwnerId()).isEqualTo(ownerId);
        assertThat(savedDocument.getProfileId()).isEqualTo(profileId);
        assertThat(savedDocument.getJobId()).isEqualTo(jobId);
        assertThat(savedDocument.getDocumentType()).isEqualTo("RESUME");
        assertThat(savedDocument.getContentType()).isEqualTo("text/markdown");
        assertThat(savedDocument.getStorageBucket()).isEqualTo("careerflow-documents");
        assertThat(savedDocument.getStorageKey()).isEqualTo(storageKeyCaptor.getValue());

        assertThat(response.id()).isEqualTo(documentId);
        assertThat(response.documentType()).isEqualTo("RESUME");
        assertThat(response.storageBucket()).isEqualTo("careerflow-documents");
        assertThat(response.storageKey()).isEqualTo(storageKeyCaptor.getValue());
    }

    @Test
    void findAllShouldUseProfileFilterWhenProfileIdIsProvided() {
        UUID profileId = UUID.randomUUID();
        GeneratedDocument document = document(profileId, UUID.randomUUID(), "RESUME", "key-1");
        when(repository.findByOwnerIdAndProfileId(ownerId, profileId)).thenReturn(List.of(document));

        List<DocumentResponse> result = documentService.findAll(profileId, UUID.randomUUID());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().profileId()).isEqualTo(profileId);
        verify(repository).findByOwnerIdAndProfileId(ownerId, profileId);
        verify(repository, never()).findByOwnerIdAndJobId(any(), any());
        verify(repository, never()).findByOwnerId(any());
    }

    @Test
    void findAllShouldUseJobFilterWhenOnlyJobIdIsProvided() {
        UUID jobId = UUID.randomUUID();
        GeneratedDocument document = document(UUID.randomUUID(), jobId, "COVER_LETTER", "key-2");
        when(repository.findByOwnerIdAndJobId(ownerId, jobId)).thenReturn(List.of(document));

        List<DocumentResponse> result = documentService.findAll(null, jobId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().jobId()).isEqualTo(jobId);
        verify(repository).findByOwnerIdAndJobId(ownerId, jobId);
        verify(repository, never()).findByOwnerId(any());
    }

    @Test
    void findAllShouldReturnAllDocumentsWhenNoFiltersAreProvided() {
        when(repository.findByOwnerId(ownerId)).thenReturn(List.of(
                document(UUID.randomUUID(), UUID.randomUUID(), "RESUME", "key-1"),
                document(UUID.randomUUID(), UUID.randomUUID(), "COVER_LETTER", "key-2")
        ));

        List<DocumentResponse> result = documentService.findAll(null, null);

        assertThat(result).hasSize(2);
        verify(repository).findByOwnerId(ownerId);
    }

    @Test
    void findByIdShouldReturnMappedResponseWhenDocumentExists() {
        UUID id = UUID.randomUUID();
        GeneratedDocument document = document(UUID.randomUUID(), UUID.randomUUID(), "RESUME", "key");
        document.setOwnerId(ownerId);
        ReflectionTestUtils.setField(document, "id", id);
        when(repository.findById(id)).thenReturn(Optional.of(document));

        DocumentResponse response = documentService.findById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.documentType()).isEqualTo("RESUME");
    }

    @Test
    void findByIdShouldThrowWhenDocumentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found: " + id);
    }

    @Test
    void findByIdShouldThrowWhenDocumentBelongsToAnotherOwner() {
        UUID id = UUID.randomUUID();
        GeneratedDocument document = document(UUID.randomUUID(), UUID.randomUUID(), "RESUME", "key");
        document.setOwnerId(UUID.randomUUID());
        ReflectionTestUtils.setField(document, "id", id);
        when(repository.findById(id)).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> documentService.findById(id))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Document access denied");
    }

    @Test
    void getContentShouldDownloadTextByStorageKey() {
        UUID id = UUID.randomUUID();
        GeneratedDocument document = document(UUID.randomUUID(), UUID.randomUUID(), "RESUME", "storage/key.md");
        document.setOwnerId(ownerId);
        when(repository.findById(id)).thenReturn(Optional.of(document));
        when(storageService.downloadText("storage/key.md")).thenReturn("stored content");

        String content = documentService.getContent(id);

        assertThat(content).isEqualTo("stored content");
        verify(storageService).downloadText("storage/key.md");
    }

    @Test
    void deleteShouldRemoveObjectFromStorageAndRepository() {
        UUID id = UUID.randomUUID();
        GeneratedDocument document = document(UUID.randomUUID(), UUID.randomUUID(), "RESUME", "storage/key.md");
        document.setOwnerId(ownerId);
        when(repository.findById(id)).thenReturn(Optional.of(document));

        documentService.delete(id);

        verify(storageService).delete("storage/key.md");
        verify(repository).delete(document);
    }

    @Test
    void downloadPdfShouldConvertDownloadedContentToPdf() {
        UUID id = UUID.randomUUID();
        GeneratedDocument document = document(UUID.randomUUID(), UUID.randomUUID(), "RESUME", "storage/key.md");
        document.setOwnerId(ownerId);
        byte[] expectedPdf = new byte[]{1, 2, 3};
        when(repository.findById(id)).thenReturn(Optional.of(document));
        when(storageService.downloadText("storage/key.md")).thenReturn("# Resume");
        when(pdfExportService.toPdf("# Resume")).thenReturn(expectedPdf);

        byte[] actual = documentService.downloadPdf(id);

        assertThat(actual).isEqualTo(expectedPdf);
        verify(pdfExportService).toPdf("# Resume");
    }

    @Test
    void downloadDocxShouldConvertDownloadedContentToDocx() {
        UUID id = UUID.randomUUID();
        GeneratedDocument document = document(UUID.randomUUID(), UUID.randomUUID(), "RESUME", "storage/key.md");
        document.setOwnerId(ownerId);
        byte[] expectedDocx = new byte[]{4, 5, 6};
        when(repository.findById(id)).thenReturn(Optional.of(document));
        when(storageService.downloadText("storage/key.md")).thenReturn("# Resume");
        when(docxExportService.toDocx("# Resume")).thenReturn(expectedDocx);

        byte[] actual = documentService.downloadDocx(id);

        assertThat(actual).isEqualTo(expectedDocx);
        verify(docxExportService).toDocx("# Resume");
    }

    @Test
    void saveFromEventShouldSkipWhenEventAlreadyProcessed() {
        UUID eventId = UUID.randomUUID();
        when(processedEventRepository.existsById(eventId)).thenReturn(true);

        documentService.saveFromEvent(new DocumentGeneratedEvent(
                eventId,
                ownerId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESUME",
                "content",
                Instant.now()
        ));

        verify(processedEventRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void saveFromEventShouldPersistDocumentForNewEvent() {
        UUID eventId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        when(processedEventRepository.existsById(eventId)).thenReturn(false);
        when(storageService.uploadText(any(String.class), any(String.class))).thenReturn("careerflow-documents");
        when(repository.save(any(GeneratedDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        documentService.saveFromEvent(new DocumentGeneratedEvent(
                eventId,
                ownerId,
                profileId,
                jobId,
                "RESUME",
                "# Resume",
                Instant.now()
        ));

        verify(processedEventRepository).save(any(ProcessedEvent.class));
        verify(repository).save(any(GeneratedDocument.class));
    }

    @Test
    void saveFromEventShouldIgnoreDuplicateSourceEventId() {
        UUID eventId = UUID.randomUUID();
        when(processedEventRepository.existsById(eventId)).thenReturn(false);
        when(storageService.uploadText(any(String.class), any(String.class))).thenReturn("careerflow-documents");
        when(repository.save(any(GeneratedDocument.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        documentService.saveFromEvent(new DocumentGeneratedEvent(
                eventId,
                ownerId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESUME",
                "content",
                Instant.now()
        ));

        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    private GeneratedDocument document(UUID profileId, UUID jobId, String type, String storageKey) {
        GeneratedDocument document = new GeneratedDocument();
        ReflectionTestUtils.setField(document, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(document, "createdAt", Instant.parse("2026-05-23T10:00:00Z"));
        document.setProfileId(profileId);
        document.setJobId(jobId);
        document.setDocumentType(type);
        document.setFileName(type.toLowerCase() + ".md");
        document.setContentType("text/markdown");
        document.setStorageBucket("careerflow-documents");
        document.setStorageKey(storageKey);
        return document;
    }
}
