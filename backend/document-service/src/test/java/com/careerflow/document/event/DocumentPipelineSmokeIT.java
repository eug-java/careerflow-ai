package com.careerflow.document.event;

import com.careerflow.common.event.DocumentGeneratedEvent;
import com.careerflow.document.repository.GeneratedDocumentRepository;
import com.careerflow.document.repository.ProcessedEventRepository;
import com.careerflow.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.kafka.listener.auto-startup=false"
})
@Testcontainers(disabledWithoutDocker = true)
class DocumentPipelineSmokeIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("careerflow_document")
            .withUsername("careerflow")
            .withPassword("careerflow");

    @Container
    static GenericContainer<?> minio = new GenericContainer<>(DockerImageName.parse("minio/minio:RELEASE.2025-09-07T16-13-09Z"))
            .withEnv("MINIO_ROOT_USER", "careerflow")
            .withEnv("MINIO_ROOT_PASSWORD", "careerflow123")
            .withCommand("server", "/data")
            .withExposedPorts(9000);

    @MockBean
    private DocumentGeneratedEventConsumer documentGeneratedEventConsumer;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("careerflow.minio.endpoint",
                () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("careerflow.minio.access-key", () -> "careerflow");
        registry.add("careerflow.minio.secret-key", () -> "careerflow123");
        registry.add("careerflow.minio.bucket", () -> "careerflow-documents");
    }

    @Autowired
    private DocumentService documentService;

    @Autowired
    private GeneratedDocumentRepository documentRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void saveFromEventPersistsToPostgresAndMinioAndSkipsDuplicates() {
        UUID eventId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        DocumentGeneratedEvent event = new DocumentGeneratedEvent(
                eventId,
                ownerId,
                profileId,
                jobId,
                "RESUME",
                "# Smoke test resume",
                Instant.now()
        );

        documentService.saveFromEvent(event);

        assertThat(documentRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.existsById(eventId)).isTrue();
        assertThat(documentRepository.findAll().getFirst().getOwnerId()).isEqualTo(ownerId);

        documentService.saveFromEvent(event);

        assertThat(documentRepository.count()).isEqualTo(1);
    }
}
