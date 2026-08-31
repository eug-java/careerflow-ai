package com.careerflow.workflow.controller;

import com.careerflow.common.test.TestAuthSupport;
import com.careerflow.workflow.client.JobClient;
import com.careerflow.workflow.client.ProfileClient;
import com.careerflow.workflow.dto.StartDocumentGenerationWorkflowRequest;
import com.careerflow.workflow.dto.WorkflowListItem;
import com.careerflow.workflow.dto.WorkflowStatus;
import com.careerflow.workflow.service.WorkflowStatusService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowControllerTest {

    @Mock
    private io.camunda.zeebe.client.ZeebeClient zeebeClient;

    @Mock
    private WorkflowStatusService workflowStatusService;

    @Mock
    private ProfileClient profileClient;

    @Mock
    private JobClient jobClient;

    @InjectMocks
    private WorkflowController controller;

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
    }

    @Test
    void startDocumentGenerationRejectsInaccessibleProfile() {
        TestAuthSupport.authenticateTestUser();
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        StartDocumentGenerationWorkflowRequest request =
                new StartDocumentGenerationWorkflowRequest(profileId, jobId, "RESUME");
        WebClientResponseException notFound = WebClientResponseException.create(
                404,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8
        );
        doThrow(notFound).when(profileClient).assertAccessible(profileId);

        assertThatThrownBy(() -> controller.startDocumentGeneration(request))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getReason()).contains("not found or not accessible");
                });

        verify(profileClient).assertAccessible(profileId);
    }

    @Test
    void getStatusDelegatesToService() {
        TestAuthSupport.authenticateTestUser();
        WorkflowStatus expected = new WorkflowStatus(100L, "document-generation-process", "RUNNING", "Started");
        when(workflowStatusService.getStatus(100L)).thenReturn(expected);

        WorkflowStatus actual = controller.getStatus(100L);

        assertThat(actual).isEqualTo(expected);
        verify(workflowStatusService).getStatus(100L);
    }

    @Test
    void listWorkflowsDelegatesToService() {
        List<WorkflowListItem> expected = List.of(
                new WorkflowListItem(100L, "document-generation-process", "RUNNING", "Started", Instant.now())
        );
        when(workflowStatusService.listForCurrentUser(null)).thenReturn(expected);

        List<WorkflowListItem> actual = controller.listWorkflows(null);

        assertThat(actual).isEqualTo(expected);
        verify(workflowStatusService).listForCurrentUser(null);
    }
}
