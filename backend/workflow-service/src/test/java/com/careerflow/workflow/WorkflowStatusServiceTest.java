package com.careerflow.workflow;

import com.careerflow.common.api.ForbiddenException;
import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.test.TestAuthSupport;
import com.careerflow.workflow.dto.WorkflowStatus;
import com.careerflow.workflow.entity.WorkflowStatusEntity;
import com.careerflow.workflow.repository.WorkflowStatusRepository;
import com.careerflow.workflow.service.WorkflowStatusService;
import com.careerflow.workflow.websocket.WorkflowStatusWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowStatusServiceTest {
    private final WorkflowStatusWebSocketHandler webSocketHandler = mock(WorkflowStatusWebSocketHandler.class);
    private final WorkflowStatusRepository repository = mock(WorkflowStatusRepository.class);
    private final WorkflowStatusService service = new WorkflowStatusService(repository, webSocketHandler);

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TestAuthSupport.clear();
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "internal",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                )
        );
    }

    @Test
    void markStartedShouldStoreRunningStatus() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findById(100L)).thenReturn(Optional.of(
                new WorkflowStatusEntity(100L, "document-generation-process", "RUNNING", "Workflow started", UUID.randomUUID())
        ));

        service.markStarted(100L, "document-generation-process", UUID.randomUUID());
        WorkflowStatus status = service.getStatus(100L);

        assertThat(status.status()).isEqualTo("RUNNING");
        assertThat(status.processInstanceKey()).isEqualTo(100L);
    }

    @Test
    void markCompletedShouldStoreCompletedStatus() {
        WorkflowStatusEntity entity = new WorkflowStatusEntity(
                100L, "document-generation-process", "RUNNING", "Workflow started", UUID.randomUUID()
        );
        when(repository.findById(100L)).thenReturn(Optional.of(entity));

        service.markCompleted(100L);

        assertThat(entity.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void markFailedShouldStoreFailedStatus() {
        WorkflowStatusEntity entity = new WorkflowStatusEntity(
                100L, "document-generation-process", "RUNNING", "Workflow started", UUID.randomUUID()
        );
        when(repository.findById(100L)).thenReturn(Optional.of(entity));

        service.markFailed(100L, "Generation failed");

        assertThat(entity.getStatus()).isEqualTo("FAILED");
        assertThat(entity.getMessage()).isEqualTo("Generation failed");
        verify(webSocketHandler).broadcast(eq(100L), org.mockito.ArgumentMatchers.contains("FAILED"));
    }

    @Test
    void getStatusThrowsWhenWorkflowDoesNotExist() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatus(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workflow status not found");
    }

    @Test
    void getStatusThrowsWhenOwnerDoesNotMatch() {
        SecurityContextHolder.clearContext();
        TestAuthSupport.authenticateTestUser();
        UUID otherOwner = UUID.randomUUID();
        WorkflowStatusEntity entity = new WorkflowStatusEntity(
                100L, "document-generation-process", "RUNNING", "Started", otherOwner
        );
        when(repository.findById(100L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.getStatus(100L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Workflow access denied");
    }

    @Test
    void getStatusReturnsWorkflowForMatchingOwner() {
        SecurityContextHolder.clearContext();
        UUID ownerId = TestAuthSupport.authenticateTestUser();
        WorkflowStatusEntity entity = new WorkflowStatusEntity(
                100L, "document-generation-process", "RUNNING", "Started", ownerId
        );
        when(repository.findById(100L)).thenReturn(Optional.of(entity));

        WorkflowStatus status = service.getStatus(100L);

        assertThat(status.status()).isEqualTo("RUNNING");
        assertThat(status.processInstanceKey()).isEqualTo(100L);
    }
}
