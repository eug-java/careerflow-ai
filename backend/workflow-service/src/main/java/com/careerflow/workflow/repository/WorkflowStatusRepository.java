package com.careerflow.workflow.repository;

import com.careerflow.workflow.entity.WorkflowStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowStatusRepository extends JpaRepository<WorkflowStatusEntity, Long> {

    List<WorkflowStatusEntity> findByOwnerIdOrderByUpdatedAtDesc(UUID ownerId);

    List<WorkflowStatusEntity> findByOwnerIdAndStatusOrderByUpdatedAtDesc(UUID ownerId, String status);
}
