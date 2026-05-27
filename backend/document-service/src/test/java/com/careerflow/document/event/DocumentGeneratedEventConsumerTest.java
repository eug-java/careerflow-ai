package com.careerflow.document.event;

import com.careerflow.document.dto.SaveGeneratedDocumentRequest;
import com.careerflow.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.careerflow.common.event.DocumentGeneratedEvent;


import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentGeneratedEventConsumerTest {

    @Mock
    private DocumentService documentService;

    @Test
    void consumeShouldSaveGeneratedDocumentFromKafkaEvent() {
        DocumentGeneratedEventConsumer consumer = new DocumentGeneratedEventConsumer(documentService);
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        DocumentGeneratedEvent event = new DocumentGeneratedEvent(
                UUID.randomUUID(),
                profileId,
                jobId,
                "RESUME",
                "generated content",
                Instant.now()
        );

        consumer.consume(event);

        ArgumentCaptor<SaveGeneratedDocumentRequest> requestCaptor = ArgumentCaptor.forClass(SaveGeneratedDocumentRequest.class);
        verify(documentService).save(requestCaptor.capture());
        SaveGeneratedDocumentRequest request = requestCaptor.getValue();

        assertThat(request.profileId()).isEqualTo(profileId);
        assertThat(request.jobId()).isEqualTo(jobId);
        assertThat(request.documentType()).isEqualTo("RESUME");
        assertThat(request.content()).isEqualTo("generated content");
    }
}
