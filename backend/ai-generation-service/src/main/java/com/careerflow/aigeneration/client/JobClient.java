/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class JobClient {

    private final WebClient webClient;

    public JobClient(@Qualifier("jobWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public JobResponse getJob(UUID jobId) {
        return webClient.get()
                .uri("/api/v1/jobs/{id}", jobId)
                .retrieve()
                .bodyToMono(JobResponse.class)
                .block();
    }
}
