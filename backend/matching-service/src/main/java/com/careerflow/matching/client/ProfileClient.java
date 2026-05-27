/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.matching.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class ProfileClient {

    private final WebClient webClient;

    public ProfileClient(@Qualifier("profileWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public ProfileResponse getProfile(UUID profileId) {
        return webClient.get()
                .uri("/api/v1/profiles/{id}", profileId)
                .retrieve()
                .bodyToMono(ProfileResponse.class)
                .block();
    }
}
