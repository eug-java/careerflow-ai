package com.careerflow.workflow.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowStatusEntityTest {

    @Test
    void updateChangesStatusMessageAndTimestamp() {
        WorkflowStatusEntity entity = new WorkflowStatusEntity(
                100L,
                "document-generation-process",
                "RUNNING",
                "Started",
                UUID.randomUUID()
        );

        entity.update("COMPLETED", "Done");

        assertThat(entity.getStatus()).isEqualTo("COMPLETED");
        assertThat(entity.getMessage()).isEqualTo("Done");
    }
}
