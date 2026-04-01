package com.gateway.controlplane.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "API key response model.")
public record ApiKeyResponse(
        @Schema(description = "Primary key identifier.", example = "1")
        Long id,
        @Schema(description = "Human-readable API key name.", example = "demo-key")
        String name,
        @Schema(description = "Visible key prefix for operators.", example = "gw_live_abc123")
        String keyPrefix,
        @Schema(description = "Owner or tenant of the key.", example = "presentation")
        String owner,
        @Schema(description = "Lifecycle status.", example = "active")
        String status,
        @Schema(description = "Per-minute request budget.", example = "1000")
        int rateLimitPerMinute,
        @Schema(description = "Expiration timestamp in UTC, if present.")
        Instant expiresAt,
        @Schema(description = "Last observed usage timestamp in UTC, if present.")
        Instant lastUsedAt,
        @Schema(description = "Creation timestamp in UTC.")
        Instant createdAt,
        @Schema(description = "Plaintext API key returned only when the key is first created.", example = "gw_live_secret_token")
        String plainTextKey
) {
}
