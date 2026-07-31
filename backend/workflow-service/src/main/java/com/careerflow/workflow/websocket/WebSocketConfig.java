/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WorkflowStatusWebSocketHandler handler;
    private final WorkflowWebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(
            WorkflowStatusWebSocketHandler handler,
            WorkflowWebSocketAuthInterceptor authInterceptor
    ) {
        this.handler = handler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/workflows/status")
                .addInterceptors(authInterceptor)
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:5174", "http://localhost:3000");
    }
}
