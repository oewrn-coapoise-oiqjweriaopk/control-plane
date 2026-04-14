package com.gateway.controlplane.service;

import com.gateway.controlplane.dto.MetricsResponse;
import com.gateway.controlplane.dto.MetricsSnapshotRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores and aggregates metrics from data-plane nodes.
 * Keeps a bounded history of recent snapshots per node (default: 100 data points = ~15 mins at 10s intervals).
 */
@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);
    private static final int MAX_SNAPSHOTS_PER_NODE = 100;

    private final Map<String, List<MetricsSnapshotRequest>> nodeMetrics = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdateTimes = new ConcurrentHashMap<>();

    /**
     * Store a metrics snapshot from a data-plane node.
     */
    public void recordMetrics(MetricsSnapshotRequest snapshot) {
        nodeMetrics.computeIfAbsent(snapshot.nodeId(), nodeId -> Collections.synchronizedList(new ArrayList<>()))
                .add(snapshot);

        // Maintain bounded history
        List<MetricsSnapshotRequest> metrics = nodeMetrics.get(snapshot.nodeId());
        if (metrics.size() > MAX_SNAPSHOTS_PER_NODE) {
            synchronized (metrics) {
                while (metrics.size() > MAX_SNAPSHOTS_PER_NODE) {
                    metrics.remove(0);
                }
            }
        }

        lastUpdateTimes.put(snapshot.nodeId(), System.currentTimeMillis());
        log.debug("Recorded metrics for node {}: RPS={}, errors={}", 
                snapshot.nodeId(), 
                snapshot.requestsPerSecond(),
                snapshot.serverErrorRequests() + snapshot.clientErrorRequests());
    }

    /**
     * Get current metrics summary for a specific node.
     * Aggregates recent snapshots to provide current state.
     */
    public Optional<MetricsResponse> getNodeMetrics(String nodeId) {
        List<MetricsSnapshotRequest> metrics = nodeMetrics.get(nodeId);
        if (metrics == null || metrics.isEmpty()) {
            return Optional.empty();
        }

        synchronized (metrics) {
            if (metrics.isEmpty()) {
                return Optional.empty();
            }

            MetricsSnapshotRequest latest = metrics.get(metrics.size() - 1);

            // Aggregate over recent snapshots for better averages
            double avgRps = metrics.stream()
                    .mapToDouble(MetricsSnapshotRequest::requestsPerSecond)
                    .average()
                    .orElse(0.0);

            double avgResponseTime = metrics.stream()
                    .mapToDouble(MetricsSnapshotRequest::averageResponseTimeMs)
                    .average()
                    .orElse(0.0);

            double p99ResponseTime = metrics.stream()
                    .mapToDouble(MetricsSnapshotRequest::p99ResponseTimeMs)
                    .average()
                    .orElse(0.0);

            double cpuUsagePercent = metrics.stream()
                    .mapToDouble(MetricsSnapshotRequest::cpuUsagePercent)
                    .average()
                    .orElse(0.0);

            long totalRequests = latest.totalRequests();
            long totalErrors = latest.serverErrorRequests() + latest.clientErrorRequests();
            double errorRate = totalRequests > 0 ? (double) totalErrors / totalRequests * 100.0 : 0.0;

            return Optional.of(new MetricsResponse(
                    nodeId,
                    avgRps,
                    totalRequests,
                    errorRate,
                    avgResponseTime,
                    p99ResponseTime,
                    cpuUsagePercent,
                    lastUpdateTimes.getOrDefault(nodeId, System.currentTimeMillis())
            ));
        }
    }

    /**
     * Get metrics for all registered nodes.
     */
    public List<MetricsResponse> getAllMetrics() {
        List<MetricsResponse> results = new ArrayList<>();
        for (String nodeId : nodeMetrics.keySet()) {
            getNodeMetrics(nodeId).ifPresent(results::add);
        }
        return results;
    }

    /**
     * Get list of active node IDs.
     */
    public List<String> getActiveNodes() {
        return new ArrayList<>(nodeMetrics.keySet());
    }

    /**
     * Clear all metrics (for testing).
     */
    public void clear() {
        nodeMetrics.clear();
        lastUpdateTimes.clear();
    }
}
