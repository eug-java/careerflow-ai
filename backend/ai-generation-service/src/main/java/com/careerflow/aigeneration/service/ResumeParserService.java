package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.dto.ParsedResumeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ResumeParserService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public ResumeParserService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public ParsedResumeResponse parse(String rawText) {
        try {
            String content = chatClient.prompt()
                    .system("""
                            You are a resume parser for US job seekers.

                            Extract structured profile data from resume text.

                            Return only valid JSON.
                            Do not wrap JSON in markdown.
                            Do not add explanations.

                            JSON schema:
                            {
                              "fullName": "string",
                              "professionalTitle": "string",
                              "email": "string",
                              "phone": "string",
                              "location": "string",
                              "locationPreference": "CITY | METRO | NATIONWIDE",
                              "summary": "string",
                              "skills": [
                                {
                                  "name": "string",
                                  "category": "string",
                                  "yearsOfExperience": number | null
                                }
                              ],
                              "experiences": [
                                {
                                  "companyName": "string",
                                  "positionTitle": "string",
                                  "location": "string",
                                  "startDate": "YYYY-MM-DD",
                                  "endDate": "YYYY-MM-DD | null",
                                  "currentPosition": true | false,
                                  "description": "string"
                                }
                              ]
                            }

                            Rules:
                            - Infer locationPreference as CITY unless resume mentions metro area or nationwide relocation.
                            - Use ISO dates when possible; if month/year only, use first day of month.
                            - Extract real skills and technologies only.
                            - Keep summary concise (3-5 sentences).
                            - Do not invent employers or skills.
                            """)
                    .user(rawText)
                    .call()
                    .content();

            return objectMapper.readValue(content, ParsedResumeResponse.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse resume with AI", ex);
        }
    }
}
