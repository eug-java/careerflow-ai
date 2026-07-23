package com.careerflow.job.repository;

import com.careerflow.job.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<JobDescription, UUID> {

    List<JobDescription> findByOwnerId(UUID ownerId);

    Optional<JobDescription> findByIdAndOwnerId(UUID id, UUID ownerId);
}
