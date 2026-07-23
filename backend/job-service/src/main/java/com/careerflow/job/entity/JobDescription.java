/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.job.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "job_descriptions")
public class JobDescription {

    @Id
    private UUID id;

    private String title;
    private String companyName;
    private String location;
    private String employmentType;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private Boolean remote;

    @Column(columnDefinition = "text")
    private String description;

    private Instant createdAt;

    private UUID ownerId;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobSkill> skills = new ArrayList<>();

    @PrePersist
    void prePersist() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }
}
