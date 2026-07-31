package com.careerflow.workflow.websocket;

import com.careerflow.workflow.entity.WorkflowStatusEntity;
import com.careerflow.workflow.repository.WorkflowStatusRepository;
import com.careerflow.workflow.security.WorkflowJwtValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
public class WorkflowWebSocketAuthInterceptor implements HandshakeInterceptor {

    private final WorkflowJwtValidator jwtValidator;
    private final WorkflowStatusRepository workflowStatusRepository;

    public WorkflowWebSocketAuthInterceptor(
            WorkflowJwtValidator jwtValidator,
            WorkflowStatusRepository workflowStatusRepository
    ) {
        this.jwtValidator = jwtValidator;
        this.workflowStatusRepository = workflowStatusRepository;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Long processInstanceKey = extractProcessInstanceKey(request);
        String token = extractToken(request);
        if (processInstanceKey == null || token == null) {
            return false;
        }

        UUID userId = jwtValidator.validateAndExtractUserId(token);
        WorkflowStatusEntity workflow = workflowStatusRepository.findById(processInstanceKey).orElse(null);
        if (workflow == null || workflow.getOwnerId() == null || !workflow.getOwnerId().equals(userId)) {
            return false;
        }

        attributes.put("processInstanceKey", processInstanceKey);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // No-op
    }

    private Long extractProcessInstanceKey(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query == null) {
            return null;
        }
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && "processInstanceKey".equals(kv[0])) {
                try {
                    return Long.parseLong(kv[1]);
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
        }
        return null;
    }

    private String extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String authHeader = servletRequest.getServletRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring("Bearer ".length());
            }
        }
        String query = request.getURI().getQuery();
        if (query == null) {
            return null;
        }
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && "token".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}
