package com.careerflow.workflow.event;

import com.careerflow.common.event.DocumentGeneratedEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentGeneratedEventPublisherTest {

    @Test
    @SuppressWarnings("unchecked")
    void publishSendsEventToConfiguredTopic() {
        KafkaTemplate<String, DocumentGeneratedEvent> kafkaTemplate = mock(KafkaTemplate.class);
        DocumentGeneratedEventPublisher publisher = new DocumentGeneratedEventPublisher(kafkaTemplate, "document.generated");

        UUID profileId = UUID.randomUUID();
        DocumentGeneratedEvent event = new DocumentGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                profileId,
                UUID.randomUUID(),
                "RESUME",
                "content",
                Instant.now()
        );
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("document.generated", 0), 0L, 0, 0L, 0, 0);
        SendResult<String, DocumentGeneratedEvent> sendResult = new SendResult<>(null, metadata);
        when(kafkaTemplate.send(eq("document.generated"), eq(profileId.toString()), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        publisher.publish(event);

        verify(kafkaTemplate).send("document.generated", profileId.toString(), event);
    }
}
