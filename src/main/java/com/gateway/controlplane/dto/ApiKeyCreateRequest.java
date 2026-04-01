package com.gateway.controlplane.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Schema(description = "Request payload for creating a new API key.")
public record ApiKeyCreateRequest(
        @Schema(description = "Human-readable API key name.", example = "demo-key")
        @NotBlank String name,
        @Schema(description = "Owner or tenant of the API key.", example = "presentation")
        @NotBlank String owner,
        @Schema(description = "Per-minute request budget for the key.", example = "1000")
        @Min(0) int rateLimitPerMinute,
        @Schema(description = "Optional expiration timestamp in UTC.", example = "2026-12-31T00:00:00Z")
        Instant expiresAt
) {
}
