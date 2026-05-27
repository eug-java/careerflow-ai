/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.config;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeClientConfig {
    @Bean
    public ZeebeClient zeebeClient(@Value("${camunda.client.zeebe.grpc-address}") String grpcAddress) {
        return ZeebeClient.newClientBuilder()
                .gatewayAddress(grpcAddress.replace("http://", "").replace("https://", ""))
                .usePlaintext()
                .build();
    }
}
