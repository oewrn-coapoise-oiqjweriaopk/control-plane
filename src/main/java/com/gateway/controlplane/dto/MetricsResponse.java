package com.gateway.controlplane.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Current metrics summary for a data-plane node.
 * Aggregates recent snapshots to provide current RPS, error rates, and latencies.
 */
public record MetricsResponse(
        @JsonProperty("nodeId")
        String nodeId,
        @JsonProperty("requestsPerSecond")
        double requestsPerSecond,
        @JsonProperty("totalRequests")
        long totalRequests,
        @JsonProperty("errorRate")
        double errorRate,
        @JsonProperty("averageResponseTimeMs")
        double averageResponseTimeMs,
        @JsonProperty("p99ResponseTimeMs")
        double p99ResponseTimeMs,
        @JsonProperty("cpuUsagePercent")
        double cpuUsagePercent,
        @JsonProperty("lastUpdated")
        long lastUpdated
) {
}
