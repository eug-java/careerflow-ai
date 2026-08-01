package com.careerflow.aigeneration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_credentials")
public class AiCredentialEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID ownerId;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false, columnDefinition = "text")
    private String encryptedApiKey;

    @Column(nullable = false)
    private String preferredModel;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected AiCredentialEntity() {
    }

    public AiCredentialEntity(
            UUID id,
            UUID ownerId,
            String provider,
            String encryptedApiKey,
            String preferredModel
    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.provider = provider;
        this.encryptedApiKey = encryptedApiKey;
        this.preferredModel = preferredModel;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String provider, String encryptedApiKey, String preferredModel) {
        this.provider = provider;
        this.encryptedApiKey = encryptedApiKey;
        this.preferredModel = preferredModel;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getProvider() {
        return provider;
    }

    public String getEncryptedApiKey() {
        return encryptedApiKey;
    }

    public String getPreferredModel() {
        return preferredModel;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
