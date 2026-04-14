package com.gateway.controlplane.dto;

import java.time.Instant;

public record RuntimeApiKeyProjection(
        Long id,
        String name,
        String keyPrefix,
        String keyHash,
        String owner,
        String status,
        int rateLimitPerMinute,
        Instant expiresAt,
        Instant lastUsedAt,
        Instant createdAt
) {
}
