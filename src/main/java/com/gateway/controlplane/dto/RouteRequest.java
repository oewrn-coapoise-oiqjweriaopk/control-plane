package com.gateway.controlplane.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for creating or updating a route.")
public record RouteRequest(
        @Schema(description = "Route name.", example = "demo-route")
        @NotBlank String name,
        @Schema(description = "HTTP method matched by the route.", example = "GET")
        @NotBlank String method,
        @Schema(description = "Path pattern matched by the route.", example = "/api/v1/demo")
        @NotBlank String pathPattern,
        @Schema(description = "Upstream URL or service target.", example = "http://demo-service:9000")
        @NotBlank String upstreamUrl,
        @Schema(description = "Whether the route requires API key authentication.", example = "true")
        boolean requiresAuth,
        @Schema(description = "Whether caching is enabled for the route.", example = "false")
        boolean cacheEnabled,
        @Schema(description = "Per-minute rate limit for the route.", example = "100")
        @Min(0) int rateLimitPerMinute,
        @Schema(description = "Operational route status.", example = "healthy")
        @NotBlank String status,
        @Schema(description = "Upstream timeout in milliseconds.", example = "2000")
        @Min(1) int timeoutMillis,
        @Schema(description = "Path prefix stripped before proxying upstream.", example = "/api")
        @NotBlank String stripPrefix
) {
}
