package com.careerflow.document.repository;

import com.careerflow.document.entity.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, UUID> {

    List<GeneratedDocument> findByOwnerId(UUID ownerId);

    List<GeneratedDocument> findByOwnerIdAndProfileId(UUID ownerId, UUID profileId);

    List<GeneratedDocument> findByOwnerIdAndJobId(UUID ownerId, UUID jobId);
}
