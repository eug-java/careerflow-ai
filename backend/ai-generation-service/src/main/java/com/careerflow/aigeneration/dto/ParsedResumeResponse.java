package com.careerflow.aigeneration.dto;

import java.math.BigDecimal;
import java.util.List;

public record ParsedResumeResponse(
        String fullName,
        String professionalTitle,
        String email,
        String phone,
        String location,
        String locationPreference,
        String summary,
        List<ParsedSkill> skills,
        List<ParsedExperience> experiences
) {
    public record ParsedSkill(
            String name,
            String category,
            BigDecimal yearsOfExperience
    ) {
    }

    public record ParsedExperience(
            String companyName,
            String positionTitle,
            String location,
            String startDate,
            String endDate,
            boolean currentPosition,
            String description
    ) {
    }
}
