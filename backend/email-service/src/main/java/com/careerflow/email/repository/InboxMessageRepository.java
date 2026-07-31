package com.careerflow.email.repository;

import com.careerflow.email.dto.EmailCategory;
import com.careerflow.email.entity.InboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InboxMessageRepository extends JpaRepository<InboxMessageEntity, UUID> {

    List<InboxMessageEntity> findByOwnerIdOrderByReceivedAtDesc(UUID ownerId);

    List<InboxMessageEntity> findByOwnerIdAndCategoryOrderByReceivedAtDesc(UUID ownerId, EmailCategory category);

    Optional<InboxMessageEntity> findByOwnerIdAndFolderAndMessageUid(UUID ownerId, String folder, long messageUid);

    long countByOwnerId(UUID ownerId);

    long countByOwnerIdAndCategory(UUID ownerId, EmailCategory category);
}
