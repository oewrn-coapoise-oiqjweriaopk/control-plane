package com.gateway.controlplane.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Heartbeat payload emitted by a microservice or data-plane node.")
public record NodeHeartbeatRequest(
        @Schema(description = "Unique node or service identifier.", example = "dummy-ms-01")
        @NotBlank String nodeId,
        @Schema(description = "Deployment region or zone.", example = "local")
        String region,
        @Schema(description = "Current CPU usage percent for this node.", example = "12")
        Integer cpuUsage,
        @Schema(description = "Current memory usage percent for this node.", example = "33")
        Integer memoryUsage,
        @Schema(description = "Active client connections observed by this node.", example = "5")
        Long activeConnections
) {
}
