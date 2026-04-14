package com.gateway.controlplane.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "A log entry from the data-plane.")
public record LogEntryResponse(
        @Schema(description = "Log entry ID", example = "1")
        Long id,

        @Schema(description = "Timestamp when log was emitted", example = "2025-04-14T10:30:45")
        LocalDateTime timestamp,

        @Schema(description = "Log level (INFO, WARN, ERROR, DEBUG)", example = "INFO")
        String level,

        @Schema(description = "Node/gateway ID", example = "gw-01")
        String nodeId,

        @Schema(description = "Log message", example = "Route /api/users/:id — 200 OK — 12ms")
        String message,

        @Schema(description = "Optional request ID for tracing", example = "req-123")
        String requestId,

        @Schema(description = "HTTP method", example = "GET")
        String method,

        @Schema(description = "Request path", example = "/api/users/123")
        String path,

        @Schema(description = "HTTP status code", example = "200")
        Integer statusCode,

        @Schema(description = "Response time in milliseconds", example = "45")
        Long responseTimeMs,

        @Schema(description = "Optional error details", example = "Connection timeout")
        String error
) {
}
