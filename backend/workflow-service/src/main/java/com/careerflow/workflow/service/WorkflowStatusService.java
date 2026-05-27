/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.service;

import com.careerflow.workflow.dto.WorkflowStatus;
import com.careerflow.workflow.websocket.WorkflowStatusWebSocketHandler;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkflowStatusService {
    private static final String DEFAULT_PROCESS_ID = "document-generation-process";
    private final Map<Long, WorkflowStatus> statuses = new ConcurrentHashMap<>();
    private final WorkflowStatusWebSocketHandler webSocketHandler;

    public WorkflowStatusService(WorkflowStatusWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    public void markStarted(long processInstanceKey, String processId) {
        WorkflowStatus status = new WorkflowStatus(processInstanceKey, processId, "RUNNING", "Workflow started");
        statuses.put(processInstanceKey, status);
        publish(status);
    }

    public void markCompleted(long processInstanceKey) {
        WorkflowStatus current = statuses.get(processInstanceKey);
        WorkflowStatus status = new WorkflowStatus(processInstanceKey,
                current == null ? DEFAULT_PROCESS_ID : current.processId(),
                "COMPLETED", "Document generation completed");
        statuses.put(processInstanceKey, status);
        publish(status);
    }

    public void markFailed(long processInstanceKey, String message) {
        WorkflowStatus current = statuses.get(processInstanceKey);
        WorkflowStatus status = new WorkflowStatus(processInstanceKey,
                current == null ? DEFAULT_PROCESS_ID : current.processId(),
                "FAILED", message);
        statuses.put(processInstanceKey, status);
        publish(status);
    }

    public WorkflowStatus getStatus(long processInstanceKey) {
        return statuses.getOrDefault(processInstanceKey,
                new WorkflowStatus(processInstanceKey, DEFAULT_PROCESS_ID, "UNKNOWN", "Workflow status not found"));
    }

    private void publish(WorkflowStatus status) {
        String json = """
                {"processInstanceKey":%d,"processId":"%s","status":"%s","message":"%s"}
                """.formatted(status.processInstanceKey(), escape(status.processId()), escape(status.status()), escape(status.message()));
        webSocketHandler.broadcast(json);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
