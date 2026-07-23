package com.careerflow.workflow.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkflowStatusWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> subscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long processInstanceKey = extractProcessInstanceKey(session);
        if (processInstanceKey == null) {
            closeQuietly(session);
            return;
        }
        subscriptions.computeIfAbsent(processInstanceKey, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        subscriptions.values().forEach(set -> set.remove(session));
    }

    public void broadcast(long processInstanceKey, String message) {
        Set<WebSocketSession> sessions = subscriptions.get(processInstanceKey);
        if (sessions == null) {
            return;
        }
        sessions.removeIf(s -> !s.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception ex) {
                sessions.remove(session);
            }
        }
    }

    private Long extractProcessInstanceKey(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        for (String part : uri.getQuery().split("&")) {
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

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close(CloseStatus.BAD_DATA);
        } catch (Exception ignored) {
        }
    }
}
