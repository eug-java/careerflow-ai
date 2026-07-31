package com.careerflow.workflow.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkflowStatusWebSocketHandlerTest {

    private final WorkflowStatusWebSocketHandler handler = new WorkflowStatusWebSocketHandler();

    @Test
    void broadcastSendsMessageToSubscribedSession() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/workflows/status?processInstanceKey=100"));
        when(session.isOpen()).thenReturn(true);
        AtomicReference<TextMessage> sent = new AtomicReference<>();
        doAnswer(invocation -> {
            sent.set(invocation.getArgument(0));
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        handler.afterConnectionEstablished(session);
        handler.broadcast(100L, "{\"status\":\"RUNNING\"}");

        assertThat(sent.get()).isNotNull();
        assertThat(sent.get().getPayload()).isEqualTo("{\"status\":\"RUNNING\"}");
    }

    @Test
    void connectionWithoutProcessInstanceKeyIsClosed() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/workflows/status"));

        handler.afterConnectionEstablished(session);

        verify(session).close(CloseStatus.BAD_DATA);
    }

    @Test
    void afterConnectionClosedRemovesSession() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getUri()).thenReturn(URI.create("ws://localhost/ws/workflows/status?processInstanceKey=200"));
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        handler.broadcast(200L, "{\"status\":\"COMPLETED\"}");

        verify(session, never()).sendMessage(any());
    }
}
