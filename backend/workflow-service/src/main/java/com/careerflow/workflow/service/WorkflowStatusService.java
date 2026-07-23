package com.careerflow.workflow.service;

import com.careerflow.common.api.ForbiddenException;
import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.security.CurrentUserProvider;
import com.careerflow.common.security.InternalAuthSupport;
import com.careerflow.workflow.dto.WorkflowStatus;
import com.careerflow.workflow.entity.WorkflowStatusEntity;
import com.careerflow.workflow.repository.WorkflowStatusRepository;
import com.careerflow.workflow.websocket.WorkflowStatusWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WorkflowStatusService {
    private static final String DEFAULT_PROCESS_ID = "document-generation-process";
    private final WorkflowStatusRepository repository;
    private final WorkflowStatusWebSocketHandler webSocketHandler;

    public WorkflowStatusService(WorkflowStatusRepository repository, WorkflowStatusWebSocketHandler webSocketHandler) {
        this.repository = repository;
        this.webSocketHandler = webSocketHandler;
    }

    @Transactional
    public void markStarted(long processInstanceKey, String processId, UUID ownerId) {
        WorkflowStatusEntity entity = new WorkflowStatusEntity(processInstanceKey, processId, "RUNNING", "Workflow started", ownerId);
        repository.save(entity);
        publish(toDto(entity));
    }

    @Transactional
    public void markCompleted(long processInstanceKey) {
        WorkflowStatusEntity entity = requireEntity(processInstanceKey);
        entity.update("COMPLETED", "Document generation completed");
        publish(toDto(entity));
    }

    @Transactional
    public void markFailed(long processInstanceKey, String message) {
        WorkflowStatusEntity entity = requireEntity(processInstanceKey);
        entity.update("FAILED", message);
        publish(toDto(entity));
    }

    @Transactional(readOnly = true)
    public WorkflowStatus getStatus(long processInstanceKey) {
        WorkflowStatusEntity entity = requireEntity(processInstanceKey);
        if (!InternalAuthSupport.isInternalCall()) {
            UUID ownerId = CurrentUserProvider.requireUserId();
            if (entity.getOwnerId() != null && !ownerId.equals(entity.getOwnerId())) {
                throw new ForbiddenException("Workflow access denied");
            }
        }
        return toDto(entity);
    }

    private WorkflowStatusEntity requireEntity(long processInstanceKey) {
        return repository.findById(processInstanceKey)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow status not found: " + processInstanceKey));
    }

    private WorkflowStatus toDto(WorkflowStatusEntity entity) {
        return new WorkflowStatus(entity.getProcessInstanceKey(), entity.getProcessId(), entity.getStatus(), entity.getMessage());
    }

    private void publish(WorkflowStatus status) {
        String json = """
                {"processInstanceKey":%d,"processId":"%s","status":"%s","message":"%s"}
                """.formatted(status.processInstanceKey(), escape(status.processId()), escape(status.status()), escape(status.message()));
        webSocketHandler.broadcast(status.processInstanceKey(), json);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
