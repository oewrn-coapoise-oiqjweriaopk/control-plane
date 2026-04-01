package com.gateway.controlplane.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record ApiKeyCreateRequest(
        @NotBlank String name,
        @NotBlank String owner,
        @Min(0) int rateLimitPerMinute,
        Instant expiresAt
) {
}
