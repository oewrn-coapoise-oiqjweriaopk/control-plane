package com.gateway.controlplane.dto;

import java.time.Instant;

public record ApiKeyResponse(
        Long id,
        String name,
        String keyPrefix,
        String owner,
        String status,
        int rateLimitPerMinute,
        Instant expiresAt,
        Instant lastUsedAt,
        Instant createdAt,
        String plainTextKey
) {
}
