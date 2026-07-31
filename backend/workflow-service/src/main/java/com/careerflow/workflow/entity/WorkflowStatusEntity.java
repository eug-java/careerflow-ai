package com.careerflow.workflow.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_statuses")
public class WorkflowStatusEntity {

    @Id
    private Long processInstanceKey;

    @Column(nullable = false)
    private String processId;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "text")
    private String message;

    private UUID ownerId;

    @Column(nullable = false)
    private Instant updatedAt;

    protected WorkflowStatusEntity() {
    }

    public WorkflowStatusEntity(Long processInstanceKey, String processId, String status, String message, UUID ownerId) {
        this.processInstanceKey = processInstanceKey;
        this.processId = processId;
        this.status = status;
        this.message = message;
        this.ownerId = ownerId;
        this.updatedAt = Instant.now();
    }

    public void update(String status, String message) {
        this.status = status;
        this.message = message;
        this.updatedAt = Instant.now();
    }

    public Long getProcessInstanceKey() {
        return processInstanceKey;
    }

    public String getProcessId() {
        return processId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}
