/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.dto.ParsedJobDescriptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class JobDescriptionParserService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public JobDescriptionParserService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public ParsedJobDescriptionResponse parse(String rawText) {
        try {
            String content = chatClient.prompt()
                    .system("""
                            You are a job description parser.

                            Extract structured data from a raw job description.

                            Return only valid JSON.
                            Do not wrap JSON in markdown.
                            Do not add explanations.

                            JSON schema:
                            {
                              "title": "string",
                              "companyName": "string",
                              "location": "string",
                              "employmentType": "string",
                              "salaryMin": number | null,
                              "salaryMax": number | null,
                              "currency": "string",
                              "remote": true | false,
                              "description": "string",
                              "skills": [
                                {
                                  "name": "string",
                                  "required": true | false
                                }
                              ]
                            }

                            Rules:
                            - If company is missing, use "Unknown Company".
                            - If location is missing, use "Unknown Location".
                            - If employment type is missing, use "Full-time".
                            - If salary is missing, use null for salaryMin and salaryMax.
                            - If currency is missing, use "USD".
                            - Keep description concise but complete.
                            - Extract only real skills from the provided text.
                            - Do not invent technologies.
                            """)
                    .user(rawText)
                    .call()
                    .content();

            return objectMapper.readValue(
                    content,
                    ParsedJobDescriptionResponse.class
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse job description with AI", e);
        }
    }
}
