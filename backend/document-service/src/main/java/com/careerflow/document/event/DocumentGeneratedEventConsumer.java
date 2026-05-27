/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.document.event;

import com.careerflow.document.dto.SaveGeneratedDocumentRequest;
import com.careerflow.document.service.DocumentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.careerflow.common.event.DocumentGeneratedEvent;

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
        documentService.save(
                new SaveGeneratedDocumentRequest(
                        event.profileId(),
                        event.jobId(),
                        event.documentType(),
                        event.content()
                )
        );
    }
}
