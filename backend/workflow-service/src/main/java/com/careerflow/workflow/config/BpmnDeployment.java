/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.workflow.config;

import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class BpmnDeployment {

    private static final Logger log =
            LoggerFactory.getLogger(BpmnDeployment.class);

    private final ZeebeClient zeebeClient;

    public BpmnDeployment(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @PostConstruct
    public void deploy() throws Exception {

        Exception lastException = null;

        for (int attempt = 1; attempt <= 20; attempt++) {

            try (
                    var inputStream =
                            new ClassPathResource(
                                    "bpmn/document-generation-process.bpmn"
                            ).getInputStream()
            ) {

                zeebeClient.newDeployResourceCommand()
                        .addResourceStream(
                                inputStream,
                                "document-generation-process.bpmn"
                        )
                        .send()
                        .join();

                log.info(
                        "BPMN process deployed successfully on attempt {}",
                        attempt
                );

                return;

            } catch (Exception e) {

                lastException = e;

                log.warn(
                        "BPMN deployment attempt {} failed. Retrying in 5 seconds...",
                        attempt
                );

                Thread.sleep(5000);
            }
        }

        throw lastException;
    }
}
