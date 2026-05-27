/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import com.careerflow.common.event.DocumentGeneratedEvent;

import java.util.concurrent.CompletableFuture;

@Component
public class DocumentGeneratedEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(DocumentGeneratedEventPublisher.class);
    private final KafkaTemplate<String, DocumentGeneratedEvent> kafkaTemplate;
    private final String topic;

    public DocumentGeneratedEventPublisher(KafkaTemplate<String, DocumentGeneratedEvent> kafkaTemplate,
                                           @Value("${careerflow.kafka.topics.document-generated}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(DocumentGeneratedEvent event) {
        CompletableFuture<SendResult<String, DocumentGeneratedEvent>> future =
                kafkaTemplate.send(topic, event.profileId().toString(), event);

        SendResult<String, DocumentGeneratedEvent> result = future.join();
        log.info("Kafka event sent. topic={}, partition={}, offset={}",
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    }
}
