package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.client.JobResponse;
import com.careerflow.aigeneration.client.ProfileResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

final class TestData {

    static final UUID PROFILE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID JOB_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private TestData() {
    }

    static ProfileResponse profile() {
        return new ProfileResponse(
                PROFILE_ID,
                "Evgenii Buianov",
                "Java Backend Developer",
                "eug.java.dev@gmail.com",
                "+1 555 111 2222",
                "Austin, TX",
                "Backend engineer with Spring Boot and Kafka experience.",
                List.of(
                        new ProfileResponse.SkillResponse(UUID.randomUUID(), "Java", "Backend", BigDecimal.valueOf(6)),
                        new ProfileResponse.SkillResponse(UUID.randomUUID(), "Spring Boot", "Backend", BigDecimal.valueOf(5)),
                        new ProfileResponse.SkillResponse(UUID.randomUUID(), "Kafka", "Messaging", BigDecimal.valueOf(3))
                ),
                List.of(
                        new ProfileResponse.ExperienceResponse(
                                UUID.randomUUID(),
                                "Bank Project",
                                "Senior Java Developer",
                                "Remote",
                                LocalDate.of(2021, 1, 1),
                                null,
                                true,
                                "Built Java microservices with Spring Boot and Kafka."
                        )
                )
        );
    }

    static ProfileResponse profileWithoutSkillsAndExperience() {
        return new ProfileResponse(
                PROFILE_ID,
                "Evgenii Buianov",
                "Java Backend Developer",
                "eug.java.dev@gmail.com",
                null,
                "Austin, TX",
                "Backend engineer.",
                null,
                null
        );
    }

    static JobResponse job() {
        return new JobResponse(
                JOB_ID,
                "Senior Java Engineer",
                "CareerFlow",
                "Austin, TX",
                "Full-time",
                BigDecimal.valueOf(120000),
                BigDecimal.valueOf(150000),
                "USD",
                true,
                "Build scalable backend services.",
                List.of(
                        new JobResponse.JobSkillResponse(UUID.randomUUID(), "Java", true),
                        new JobResponse.JobSkillResponse(UUID.randomUUID(), "Spring Boot", true),
                        new JobResponse.JobSkillResponse(UUID.randomUUID(), "AWS", false)
                )
        );
    }
}
