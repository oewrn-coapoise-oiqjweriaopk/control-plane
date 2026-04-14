package com.gateway.controlplane.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Aggregated overview metrics for the gateway control plane.")
public record OverviewResponse(
        @Schema(description = "Total number of configured routes.", example = "8")
        long totalRoutes,
        @Schema(description = "Number of routes currently marked healthy.", example = "6")
        long healthyRoutes,
        @Schema(description = "Number of enabled policy rules.", example = "4")
        long activePolicies,
        @Schema(description = "Number of active API keys.", example = "12")
        long activeApiKeys,
        @Schema(description = "Number of nodes currently online.", example = "5")
        long onlineNodes,
        @Schema(description = "Number of control-plane admins or operators.", example = "3")
        long totalAdmins,
        @Schema(description = "Average CPU usage across nodes.", example = "41.2")
        double averageNodeCpu,
        @Schema(description = "Average memory usage across nodes.", example = "58.7")
        double averageNodeMemory,
        @Schema(description = "Total active connections across nodes.", example = "6640")
        long totalNodeConnections,
        @Schema(description = "Route counts grouped by status.")
        Map<String, Long> routesByStatus,
        @Schema(description = "Node counts grouped by status.")
        Map<String, Long> nodesByStatus,
        @Schema(description = "Recent logs from data-plane (last 50)")
        List<LogEntryResponse> recentLogs
) {
}
