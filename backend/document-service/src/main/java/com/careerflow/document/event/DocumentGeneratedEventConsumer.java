package com.careerflow.document.event;

import com.careerflow.common.event.DocumentGeneratedEvent;
import com.careerflow.document.service.DocumentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentGeneratedEventConsumer {

    private final DocumentService documentService;

    public DocumentGeneratedEventConsumer(DocumentService documentService) {
        this.documentService = documentService;
    }

    @KafkaListener(
            topics = "${careerflow.kafka.topics.document-generated}",
            groupId = "document-service"
    )
    public void consume(DocumentGeneratedEvent event) {
        documentService.saveFromEvent(event);
    }
}
