package com.careerflow.workflow.websocket;

import com.careerflow.workflow.entity.WorkflowStatusEntity;
import com.careerflow.workflow.repository.WorkflowStatusRepository;
import com.careerflow.workflow.security.WorkflowJwtValidator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowWebSocketAuthInterceptorTest {

    @Test
    void beforeHandshakeAllowsMatchingOwner() {
        UUID ownerId = UUID.randomUUID();
        WorkflowJwtValidator jwtValidator = mock(WorkflowJwtValidator.class);
        WorkflowStatusRepository repository = mock(WorkflowStatusRepository.class);
        WorkflowWebSocketAuthInterceptor interceptor = new WorkflowWebSocketAuthInterceptor(jwtValidator, repository);

        when(jwtValidator.validateAndExtractUserId("jwt-token")).thenReturn(ownerId);
        when(repository.findById(100L)).thenReturn(Optional.of(
                new WorkflowStatusEntity(100L, "document-generation-process", "RUNNING", "Started", ownerId)
        ));

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(
                new MockServletContext(),
                "GET",
                "/ws/workflows/status"
        );
        servletRequest.setQueryString("processInstanceKey=100&token=jwt-token");

        var request = new org.springframework.http.server.ServletServerHttpRequest(servletRequest);
        Map<String, Object> attributes = new HashMap<>();

        boolean allowed = interceptor.beforeHandshake(
                request,
                null,
                mock(org.springframework.web.socket.WebSocketHandler.class),
                attributes
        );

        assertThat(allowed).isTrue();
        assertThat(attributes.get("processInstanceKey")).isEqualTo(100L);
    }

    @Test
    void beforeHandshakeRejectsWhenOwnerDoesNotMatch() {
        UUID ownerId = UUID.randomUUID();
        WorkflowJwtValidator jwtValidator = mock(WorkflowJwtValidator.class);
        WorkflowStatusRepository repository = mock(WorkflowStatusRepository.class);
        WorkflowWebSocketAuthInterceptor interceptor = new WorkflowWebSocketAuthInterceptor(jwtValidator, repository);

        when(jwtValidator.validateAndExtractUserId("jwt-token")).thenReturn(ownerId);
        when(repository.findById(100L)).thenReturn(Optional.of(
                new WorkflowStatusEntity(100L, "document-generation-process", "RUNNING", "Started", UUID.randomUUID())
        ));

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(
                new MockServletContext(),
                "GET",
                "/ws/workflows/status"
        );
        servletRequest.setQueryString("processInstanceKey=100&token=jwt-token");
        var request = new org.springframework.http.server.ServletServerHttpRequest(servletRequest);

        boolean allowed = interceptor.beforeHandshake(
                request,
                null,
                mock(org.springframework.web.socket.WebSocketHandler.class),
                new HashMap<>()
        );

        assertThat(allowed).isFalse();
    }
}
