package com.careerflow.workflow.client;

import com.careerflow.common.client.ServiceWebClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ServiceClientConfig {

    @Bean
    public WebClient profileWebClient(
            @Value("${careerflow.services.profile-service-url}") String baseUrl,
            ServiceWebClientFactory serviceWebClientFactory
    ) {
        return serviceWebClientFactory.create(baseUrl);
    }

    @Bean
    public WebClient jobWebClient(
            @Value("${careerflow.services.job-service-url}") String baseUrl,
            ServiceWebClientFactory serviceWebClientFactory
    ) {
        return serviceWebClientFactory.create(baseUrl);
    }
}
