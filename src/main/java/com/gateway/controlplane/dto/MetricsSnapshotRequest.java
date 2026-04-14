package com.gateway.controlplane.dto;

/**
 * Metrics snapshot received from data-plane instances.
 * Contains RPS, request counts, and response time percentiles.
 */
public record MetricsSnapshotRequest(
        long timestamp,
        String nodeId,
        double requestsPerSecond,
        long totalRequests,
        long successfulRequests,      // 2xx
        long clientErrorRequests,     // 4xx
        long serverErrorRequests,     // 5xx
        double averageResponseTimeMs,
        double p99ResponseTimeMs,
        double p95ResponseTimeMs,
        double cpuUsagePercent
) {
}
