package com.careerflow.workflow.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeClientConfig {

    @Bean
    public ZeebeClient zeebeClient(
            @Value("${camunda.client.zeebe.grpc-address}") String grpcAddress,
            @Value("${camunda.client.zeebe.use-plaintext:true}") boolean usePlaintext
    ) {
        ZeebeClientBuilder builder = ZeebeClient.newClientBuilder()
                .gatewayAddress(grpcAddress.replace("http://", "").replace("https://", ""));
        if (usePlaintext) {
            builder.usePlaintext();
        }
        return builder.build();
    }
}
