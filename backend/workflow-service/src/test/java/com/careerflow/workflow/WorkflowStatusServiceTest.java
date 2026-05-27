package com.careerflow.workflow;

import com.careerflow.workflow.dto.WorkflowStatus;
import com.careerflow.workflow.service.WorkflowStatusService;
import com.careerflow.workflow.websocket.WorkflowStatusWebSocketHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WorkflowStatusServiceTest {
    private final WorkflowStatusWebSocketHandler webSocketHandler = mock(WorkflowStatusWebSocketHandler.class);
    private final WorkflowStatusService service = new WorkflowStatusService(webSocketHandler);

    @Test
    void markStartedShouldStoreRunningStatus() {
        service.markStarted(100L, "document-generation-process");
        WorkflowStatus status = service.getStatus(100L);
        assertThat(status.status()).isEqualTo("RUNNING");
        assertThat(status.processInstanceKey()).isEqualTo(100L);
    }

    @Test
    void markCompletedShouldStoreCompletedStatus() {
        service.markStarted(100L, "document-generation-process");
        service.markCompleted(100L);
        assertThat(service.getStatus(100L).status()).isEqualTo("COMPLETED");
    }

    @Test
    void unknownStatusShouldReturnUnknown() {
        assertThat(service.getStatus(999L).status()).isEqualTo("UNKNOWN");
    }
}
