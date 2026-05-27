/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class DocumentClient {

    private final WebClient webClient;

    public DocumentClient(@Qualifier("documentWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public DocumentResponse save(SaveDocumentRequest request) {
        return webClient.post()
                .uri("/api/v1/documents")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DocumentResponse.class)
                .block();
    }
}
