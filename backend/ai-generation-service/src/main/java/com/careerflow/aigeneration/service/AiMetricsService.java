/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class AiMetricsService {

    private final MeterRegistry meterRegistry;

    public AiMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordRequest(String documentType) {
        Counter.builder("ai_generation_requests_total")
                .description("Total AI generation requests")
                .tag("document_type", documentType)
                .register(meterRegistry)
                .increment();
    }

    public void recordSuccess(String documentType, String mode, String model) {
        Counter.builder("ai_generation_success_total")
                .description("Successful AI generation responses")
                .tag("document_type", documentType)
                .tag("mode", mode)
                .tag("model", model)
                .register(meterRegistry)
                .increment();
    }

    public void recordFallback(String documentType, String reason) {
        Counter.builder("ai_generation_fallback_total")
                .description("AI fallback generations")
                .tag("document_type", documentType)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }
}
