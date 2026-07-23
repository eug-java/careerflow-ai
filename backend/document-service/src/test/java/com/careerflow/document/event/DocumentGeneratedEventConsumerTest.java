package com.careerflow.document.event;

import com.careerflow.common.event.DocumentGeneratedEvent;
import com.careerflow.document.dto.SaveGeneratedDocumentRequest;
import com.careerflow.document.entity.ProcessedEvent;
import com.careerflow.document.repository.ProcessedEventRepository;
import com.careerflow.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentGeneratedEventConsumerTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentGeneratedEventConsumer consumer;

    @Test
    void consumeDelegatesToDocumentService() {
        DocumentGeneratedEvent event = new DocumentGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESUME",
                "content",
                Instant.now()
        );

        consumer.consume(event);

        verify(documentService).saveFromEvent(event);
    }
}
