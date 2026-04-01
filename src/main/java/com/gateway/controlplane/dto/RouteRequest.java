package com.gateway.controlplane.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RouteRequest(
        @NotBlank String name,
        @NotBlank String method,
        @NotBlank String pathPattern,
        @NotBlank String upstreamUrl,
        boolean requiresAuth,
        boolean cacheEnabled,
        @Min(0) int rateLimitPerMinute,
        @NotBlank String status,
        @Min(1) int timeoutMillis,
        @NotBlank String stripPrefix
) {
}
