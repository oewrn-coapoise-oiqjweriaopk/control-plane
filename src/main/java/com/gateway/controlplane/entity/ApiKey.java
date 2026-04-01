package com.gateway.controlplane.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.Instant;

@Entity
public class ApiKey extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String keyPrefix;

    @Column(nullable = false, unique = true, length = 128)
    private String keyHash;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String status;

    @Column
    private Instant expiresAt;

    @Column
    private Instant lastUsedAt;

    @Column(nullable = false)
    private int rateLimitPerMinute;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }
}
