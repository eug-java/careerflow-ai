package com.careerflow.common.client;

import org.springframework.beans.factory.annotation.Value;

public class InternalClientHeaders {

    public static final String HEADER = "X-Careerflow-Internal-Key";

    private final String internalApiKey;

    public InternalClientHeaders(@Value("${careerflow.internal-api-key:local-internal-key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    public String apiKey() {
        return internalApiKey;
    }
}
