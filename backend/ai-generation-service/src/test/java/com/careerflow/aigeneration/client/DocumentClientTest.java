package com.careerflow.aigeneration.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentClientTest {

    @Test
    void saveShouldPostDocumentRequestAndReturnResponse() {
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class, RETURNS_SELF);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        SaveDocumentRequest request = new SaveDocumentRequest(profileId, jobId, "RESUME", "content");
        DocumentResponse expected = new DocumentResponse(
                UUID.randomUUID(),
                profileId,
                jobId,
                "RESUME",
                "resume.md",
                "text/markdown",
                "documents",
                "key",
                Instant.parse("2026-05-23T10:00:00Z")
        );

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(request)).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(DocumentResponse.class)).thenReturn(Mono.just(expected));

        DocumentResponse actual = new DocumentClient(webClient).save(request);

        assertThat(actual).isEqualTo(expected);
        verify(requestBodyUriSpec).uri("/api/v1/documents");
        verify(requestBodyUriSpec).bodyValue(request);
    }
}
