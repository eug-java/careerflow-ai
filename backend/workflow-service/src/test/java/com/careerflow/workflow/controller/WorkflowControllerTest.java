package com.careerflow.workflow.controller;

import com.careerflow.common.test.TestAuthSupport;
import com.careerflow.workflow.dto.WorkflowStatus;
import com.careerflow.workflow.service.WorkflowStatusService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowControllerTest {

    @Mock
    private io.camunda.zeebe.client.ZeebeClient zeebeClient;

    @Mock
    private WorkflowStatusService workflowStatusService;

    @InjectMocks
    private WorkflowController controller;

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
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
}
