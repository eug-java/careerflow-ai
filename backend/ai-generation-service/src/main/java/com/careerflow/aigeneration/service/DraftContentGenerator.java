/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.client.JobResponse;
import com.careerflow.aigeneration.client.ProfileResponse;
import com.careerflow.aigeneration.dto.DocumentType;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DraftContentGenerator {

    public String generate(ProfileResponse profile, JobResponse job, DocumentType documentType) {
        return switch (documentType) {
            case COVER_LETTER -> generateCoverLetter(profile, job);
            case RESUME -> generateResume(profile, job);
        };
    }

    private String generateCoverLetter(ProfileResponse profile, JobResponse job) {
        String skills = profile.skills() == null
                ? ""
                : profile.skills().stream()
                  .map(ProfileResponse.SkillResponse::name)
                  .collect(Collectors.joining(", "));

        return """
                Dear Hiring Team,

                I am excited to apply for the %s position at %s. I am a %s based in %s with experience in %s.

                My background includes %s

                Based on the job description, I believe my experience with %s can help contribute to your engineering team.

                Thank you for your time and consideration.

                Sincerely,
                %s
                """.formatted(
                safe(job.title()),
                safe(job.companyName()),
                safe(profile.professionalTitle()),
                safe(profile.location()),
                skills,
                safe(profile.summary()),
                skills,
                safe(profile.fullName())
        );
    }

    private String generateResume(ProfileResponse profile, JobResponse job) {
        String skills = profile.skills() == null
                ? ""
                : profile.skills().stream()
                  .map(ProfileResponse.SkillResponse::name)
                  .collect(Collectors.joining(", "));

        String experience = profile.experiences() == null
                ? ""
                : profile.experiences().stream()
                  .map(exp -> "- %s, %s: %s".formatted(
                          safe(exp.positionTitle()),
                          safe(exp.companyName()),
                          safe(exp.description())
                  ))
                  .collect(Collectors.joining("\n"));

        return """
                %s
                %s
                %s | %s

                Target Role: %s at %s

                Professional Summary
                %s

                Relevant Skills
                %s

                Professional Experience
                %s
                """.formatted(
                safe(profile.fullName()),
                safe(profile.professionalTitle()),
                safe(profile.email()),
                safe(profile.location()),
                safe(job.title()),
                safe(job.companyName()),
                safe(profile.summary()),
                skills,
                experience
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
