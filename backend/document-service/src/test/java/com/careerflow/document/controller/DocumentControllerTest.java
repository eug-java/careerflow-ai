package com.careerflow.document.controller;

import com.careerflow.document.dto.DocumentResponse;
import com.careerflow.document.dto.SaveGeneratedDocumentRequest;
import com.careerflow.document.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    private DocumentController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentController(documentService);
    }

    @Test
    void saveShouldDelegateToService() {
        SaveGeneratedDocumentRequest request = new SaveGeneratedDocumentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESUME",
                "content"
        );
        DocumentResponse expected = response(UUID.randomUUID());
        when(documentService.save(request)).thenReturn(expected);

        DocumentResponse actual = controller.save(request);

        assertThat(actual).isEqualTo(expected);
        verify(documentService).save(request);
    }

    @Test
    void findAllShouldDelegateFiltersToService() {
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        List<DocumentResponse> expected = List.of(response(UUID.randomUUID()));
        when(documentService.findAll(profileId, jobId)).thenReturn(expected);

        List<DocumentResponse> actual = controller.findAll(profileId, jobId);

        assertThat(actual).isEqualTo(expected);
        verify(documentService).findAll(profileId, jobId);
    }

    @Test
    void findByIdShouldDelegateToService() {
        UUID id = UUID.randomUUID();
        DocumentResponse expected = response(id);
        when(documentService.findById(id)).thenReturn(expected);

        DocumentResponse actual = controller.findById(id);

        assertThat(actual).isEqualTo(expected);
        verify(documentService).findById(id);
    }

    @Test
    void getContentShouldDelegateToService() {
        UUID id = UUID.randomUUID();
        when(documentService.getContent(id)).thenReturn("content");

        String actual = controller.getContent(id);

        assertThat(actual).isEqualTo("content");
        verify(documentService).getContent(id);
    }

    @Test
    void deleteShouldDelegateToService() {
        UUID id = UUID.randomUUID();

        controller.delete(id);

        verify(documentService).delete(id);
    }

    @Test
    void downloadPdfShouldReturnPdfWithAttachmentHeader() {
        UUID id = UUID.randomUUID();
        byte[] pdf = new byte[]{1, 2, 3};
        when(documentService.downloadPdf(id)).thenReturn(pdf);

        ResponseEntity<byte[]> response = controller.downloadPdf(id);

        assertThat(response.getBody()).isEqualTo(pdf);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"document-%s.pdf\"".formatted(id));
    }

    @Test
    void downloadDocxShouldReturnDocxWithAttachmentHeader() {
        UUID id = UUID.randomUUID();
        byte[] docx = new byte[]{4, 5, 6};
        when(documentService.downloadDocx(id)).thenReturn(docx);

        ResponseEntity<byte[]> response = controller.downloadDocx(id);

        assertThat(response.getBody()).isEqualTo(docx);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
                .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"document-%s.docx\"".formatted(id));
    }

    private DocumentResponse response(UUID id) {
        return new DocumentResponse(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESUME",
                "resume.md",
                "text/markdown",
                "careerflow-documents",
                "storage/key.md",
                Instant.parse("2026-05-23T10:00:00Z")
        );
    }
}
