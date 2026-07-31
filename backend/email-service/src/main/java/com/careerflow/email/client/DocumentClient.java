package com.careerflow.email.client;

import com.careerflow.email.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class DocumentClient {

    private final WebClient webClient;
    private final JwtTokenProvider jwtTokenProvider;

    public DocumentClient(
            @Value("${careerflow.services.document-service-url}") String baseUrl,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public byte[] downloadPdf(UUID documentId) {
        return webClient.get()
                .uri("/api/v1/documents/{id}/pdf", documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTokenProvider.currentBearerToken())
                .accept(MediaType.APPLICATION_PDF)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    public String fetchFileName(UUID documentId) {
        return webClient.get()
                .uri("/api/v1/documents/{id}", documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTokenProvider.currentBearerToken())
                .retrieve()
                .bodyToMono(DocumentResponse.class)
                .map(DocumentResponse::fileName)
                .block();
    }

    private record DocumentResponse(String fileName) {
    }
}
