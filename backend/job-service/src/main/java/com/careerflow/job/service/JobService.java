/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.job.service;

import com.careerflow.job.dto.*;
import com.careerflow.job.entity.JobDescription;
import com.careerflow.job.entity.JobSkill;
import com.careerflow.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRepository repository;

    public JobService(JobRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public JobResponse create(CreateJobRequest request) {
        JobDescription job = new JobDescription();

        job.setTitle(request.title());
        job.setCompanyName(request.companyName());
        job.setLocation(request.location());
        job.setEmploymentType(request.employmentType());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setCurrency(request.currency());
        job.setRemote(request.remote());
        job.setDescription(request.description());

        if (request.skills() != null) {
            for (JobSkillRequest s : request.skills()) {
                JobSkill skill = new JobSkill();
                skill.setName(s.name());
                skill.setRequired(s.required());
                skill.setJob(job);
                job.getSkills().add(skill);
            }
        }

        return toResponse(repository.save(job));
    }

    @Transactional(readOnly = true)
    public JobResponse findById(UUID id) {
        JobDescription job = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        return toResponse(job);
    }
    @Transactional(readOnly = true)
    public List<JobResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(UUID id) {
        JobDescription job = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        repository.delete(job);
    }

    private JobResponse toResponse(JobDescription job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getCompanyName(),
                job.getLocation(),
                job.getEmploymentType(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getCurrency(),
                job.getRemote(),
                job.getDescription(),
                job.getSkills().stream()
                        .map(s -> new JobSkillResponse(s.getId(), s.getName(), s.isRequired()))
                        .toList(),
                job.getCreatedAt()
        );
    }

    @Transactional
    public JobResponse update(UUID id, CreateJobRequest request) {
        JobDescription job = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        job.setTitle(request.title());
        job.setCompanyName(request.companyName());
        job.setLocation(request.location());
        job.setEmploymentType(request.employmentType());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setCurrency(request.currency());
        job.setRemote(request.remote());
        job.setDescription(request.description());

        job.getSkills().clear();

        if (request.skills() != null) {
            request.skills().forEach(skillRequest -> {
                JobSkill skill = new JobSkill();
                skill.setName(skillRequest.name());
                skill.setRequired(skillRequest.required());
                skill.setJob(job);

                job.getSkills().add(skill);
            });
        }

        return toResponse(job);
    }
}
