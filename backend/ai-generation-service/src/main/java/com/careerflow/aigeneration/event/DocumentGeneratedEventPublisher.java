package com.careerflow.aigeneration.event;

import com.careerflow.common.event.DocumentGeneratedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DocumentGeneratedEventPublisher {

    private final KafkaTemplate<String, DocumentGeneratedEvent> kafkaTemplate;
    private final String topicName;

    public DocumentGeneratedEventPublisher(
            KafkaTemplate<String, DocumentGeneratedEvent> kafkaTemplate,
            @Value("${careerflow.kafka.topics.document-generated}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void publish(DocumentGeneratedEvent event) {
        kafkaTemplate.send(topicName, event.profileId().toString(), event);
    }
}
