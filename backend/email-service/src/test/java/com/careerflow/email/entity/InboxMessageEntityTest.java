package com.careerflow.email.entity;

import com.careerflow.email.dto.EmailCategory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InboxMessageEntityTest {

    @Test
    void markRepliedShouldSetRepliedAt() {
        InboxMessageEntity entity = new InboxMessageEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                10L,
                "<msg@example.com>",
                "INBOX",
                "Subject",
                "from@example.com",
                "to@example.com",
                "preview",
                "body",
                Instant.parse("2026-07-01T10:00:00Z"),
                EmailCategory.OTHER,
                "fallback"
        );

        assertThat(entity.getRepliedAt()).isNull();

        entity.markReplied();

        assertThat(entity.getRepliedAt()).isNotNull();
    }
}
