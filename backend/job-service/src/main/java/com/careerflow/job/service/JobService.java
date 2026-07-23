package com.careerflow.job.service;

import com.careerflow.common.api.ForbiddenException;
import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.security.CurrentUserProvider;
import com.careerflow.common.security.InternalAuthSupport;
import com.careerflow.job.dto.*;
import com.careerflow.job.entity.JobDescription;
import com.careerflow.job.entity.JobSkill;
import com.careerflow.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
        if (!InternalAuthSupport.isInternalCall()) {
            job.setOwnerId(CurrentUserProvider.requireUserId());
        }

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
        return toResponse(requireJob(id));
    }

    @Transactional(readOnly = true)
    public List<JobResponse> findAll() {
        if (InternalAuthSupport.isInternalCall()) {
            return repository.findAll().stream().map(this::toResponse).toList();
        }
        UUID ownerId = CurrentUserProvider.requireUserId();
        return repository.findByOwnerId(ownerId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(UUID id) {
        JobDescription job = requireJob(id);
        assertOwned(job);
        repository.delete(job);
    }

    @Transactional
    public JobResponse update(UUID id, CreateJobRequest request) {
        JobDescription job = requireJob(id);
        assertOwned(job);

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

    private JobDescription requireJob(UUID id) {
        if (InternalAuthSupport.isInternalCall()) {
            return repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        }
        UUID ownerId = CurrentUserProvider.requireUserId();
        return repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
    }

    private void assertOwned(JobDescription job) {
        if (InternalAuthSupport.isInternalCall()) {
            return;
        }
        UUID ownerId = CurrentUserProvider.requireUserId();
        if (job.getOwnerId() != null && !ownerId.equals(job.getOwnerId())) {
            throw new ForbiddenException("Job access denied");
        }
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
}
