/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.client.JobResponse;
import com.careerflow.aigeneration.client.ProfileResponse;
import com.careerflow.aigeneration.dto.DocumentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.stream.Collectors;

@Component
public class AiResumeGenerator {

    private final ChatClient chatClient;

    public AiResumeGenerator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Retry(name = "openAiRetry")
    @CircuitBreaker(name = "openAiCircuitBreaker")
    public String generate(ProfileResponse profile, JobResponse job, DocumentType documentType) {
        String prompt = buildPrompt(profile, job, documentType);

        return chatClient.prompt()
                .system("""
        You are an expert technical resume writer.

        Generate professional job application documents.

        Critical rules:
        - Use only the facts provided in the candidate profile.
        - Do not invent employers, titles, dates, degrees, certifications, metrics, projects, tools, or achievements.
        - Do not exaggerate experience level.
        - If a required job skill is not present in the candidate profile, do not claim the candidate has it.
        - You may rephrase existing experience to better align with the job description, but the meaning must remain truthful.
        - Return the document in clean Markdown.
        """)
                .user(prompt)
                .call()
                .content();
    }

    private String buildPrompt(ProfileResponse profile, JobResponse job, DocumentType documentType) {
        return """
                Document type: %s

                Candidate:
                Full name: %s
                Title: %s
                Location: %s
                Email: %s
                Summary: %s

                Skills:
                %s

                Experience:
                %s

                Target job:
                Title: %s
                Company: %s
                Location: %s
                Employment type: %s
                Description:
                %s

                Required skills:
                %s

                Task:
                Generate a tailored %s for this job.
                Keep it concise, truthful, and ATS-friendly.
                """.formatted(
                documentType,
                safe(profile.fullName()),
                safe(profile.professionalTitle()),
                safe(profile.location()),
                safe(profile.email()),
                safe(profile.summary()),
                formatSkills(profile),
                formatExperiences(profile),
                safe(job.title()),
                safe(job.companyName()),
                safe(job.location()),
                safe(job.employmentType()),
                safe(job.description()),
                formatJobSkills(job),
                documentType
        );
    }

    private String formatSkills(ProfileResponse profile) {
        if (profile.skills() == null || profile.skills().isEmpty()) {
            return "No skills provided.";
        }

        return profile.skills().stream()
                .map(skill -> "- %s (%s, %s years)".formatted(
                        safe(skill.name()),
                        safe(skill.category()),
                        skill.yearsOfExperience()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatExperiences(ProfileResponse profile) {
        if (profile.experiences() == null || profile.experiences().isEmpty()) {
            return "No experience provided.";
        }

        return profile.experiences().stream()
                .map(exp -> """
                        - %s at %s
                          Location: %s
                          Dates: %s - %s
                          Description: %s
                        """.formatted(
                        safe(exp.positionTitle()),
                        safe(exp.companyName()),
                        safe(exp.location()),
                        exp.startDate(),
                        exp.currentPosition() ? "Present" : exp.endDate(),
                        safe(exp.description())
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatJobSkills(JobResponse job) {
        if (job.skills() == null || job.skills().isEmpty()) {
            return "No explicit skills provided.";
        }

        return job.skills().stream()
                .map(skill -> "- %s%s".formatted(
                        safe(skill.name()),
                        skill.required() ? " (required)" : " (preferred)"
                ))
                .collect(Collectors.joining("\n"));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
