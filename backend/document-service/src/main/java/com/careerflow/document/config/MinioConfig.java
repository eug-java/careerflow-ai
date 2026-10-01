/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.document.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(
            @Value("${careerflow.minio.endpoint}") String endpoint,
            @Value("${careerflow.minio.access-key}") String accessKey,
            @Value("${careerflow.minio.secret-key}") String secretKey,
            @Value("${careerflow.minio.region:}") String region
    ) {
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey);
        if (region != null && !region.isBlank()) {
            builder.region(region);
        }
        return builder.build();
    }
}
