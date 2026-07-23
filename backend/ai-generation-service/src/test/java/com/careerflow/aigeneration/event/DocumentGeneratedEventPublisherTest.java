package com.careerflow.aigeneration.event;

import com.careerflow.common.event.DocumentGeneratedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DocumentGeneratedEventPublisherTest {

    @Test
    void publishShouldSendEventToConfiguredTopicUsingProfileIdAsKey() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, DocumentGeneratedEvent> kafkaTemplate = mock(KafkaTemplate.class);
        DocumentGeneratedEventPublisher publisher = new DocumentGeneratedEventPublisher(
                kafkaTemplate,
                "document.generated"
        );
        UUID profileId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        DocumentGeneratedEvent event = new DocumentGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                profileId,
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "RESUME",
                "Generated content",
                Instant.parse("2026-05-23T10:00:00Z")
        );

        publisher.publish(event);

        verify(kafkaTemplate).send("document.generated", profileId.toString(), event);
    }
}
