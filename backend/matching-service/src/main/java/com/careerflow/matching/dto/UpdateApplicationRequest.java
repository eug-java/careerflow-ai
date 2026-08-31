package com.careerflow.matching.dto;

public record UpdateApplicationRequest(
        ApplicationStatus status,
        String notes
) {
}
