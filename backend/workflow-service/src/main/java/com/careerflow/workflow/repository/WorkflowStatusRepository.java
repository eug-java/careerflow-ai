package com.careerflow.workflow.repository;

import com.careerflow.workflow.entity.WorkflowStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStatusRepository extends JpaRepository<WorkflowStatusEntity, Long> {
}
