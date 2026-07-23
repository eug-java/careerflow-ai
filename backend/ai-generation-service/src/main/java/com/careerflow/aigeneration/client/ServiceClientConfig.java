package com.careerflow.aigeneration.client;

import com.careerflow.common.client.InternalClientHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ServiceClientConfig {

    @Bean
    public WebClient profileWebClient(
            @Value("${careerflow.services.profile-service-url}") String baseUrl,
            InternalClientHeaders internalClientHeaders
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(InternalClientHeaders.HEADER, internalClientHeaders.apiKey())
                .build();
    }

    @Bean
    public WebClient jobWebClient(
            @Value("${careerflow.services.job-service-url}") String baseUrl,
            InternalClientHeaders internalClientHeaders
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(InternalClientHeaders.HEADER, internalClientHeaders.apiKey())
                .build();
    }

    @Bean
    public WebClient documentWebClient(
            @Value("${careerflow.services.document-service-url}") String baseUrl,
            InternalClientHeaders internalClientHeaders
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(InternalClientHeaders.HEADER, internalClientHeaders.apiKey())
                .build();
    }
}
