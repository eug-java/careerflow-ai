package com.careerflow.email.entity;

import com.careerflow.email.dto.EmailCategory;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inbox_messages")
public class InboxMessageEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private long messageUid;

    private String internetMessageId;

    @Column(nullable = false)
    private String folder;

    private String subject;

    private String fromAddress;

    private String toAddress;

    @Column(columnDefinition = "text")
    private String bodyPreview;

    @Column(columnDefinition = "text")
    private String bodyText;

    @Column(nullable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailCategory category;

    private String classificationReason;

    private Instant repliedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected InboxMessageEntity() {
    }

    public InboxMessageEntity(UUID id, UUID ownerId, long messageUid, String internetMessageId, String folder,
                              String subject, String fromAddress, String toAddress, String bodyPreview,
                              String bodyText, Instant receivedAt, EmailCategory category, String classificationReason) {
        this.id = id;
        this.ownerId = ownerId;
        this.messageUid = messageUid;
        this.internetMessageId = internetMessageId;
        this.folder = folder;
        this.subject = subject;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.bodyPreview = bodyPreview;
        this.bodyText = bodyText;
        this.receivedAt = receivedAt;
        this.category = category;
        this.classificationReason = classificationReason;
        this.createdAt = Instant.now();
    }

    public void markReplied() {
        this.repliedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public long getMessageUid() {
        return messageUid;
    }

    public String getInternetMessageId() {
        return internetMessageId;
    }

    public String getFolder() {
        return folder;
    }

    public String getSubject() {
        return subject;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getBodyPreview() {
        return bodyPreview;
    }

    public String getBodyText() {
        return bodyText;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public EmailCategory getCategory() {
        return category;
    }

    public String getClassificationReason() {
        return classificationReason;
    }

    public Instant getRepliedAt() {
        return repliedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
