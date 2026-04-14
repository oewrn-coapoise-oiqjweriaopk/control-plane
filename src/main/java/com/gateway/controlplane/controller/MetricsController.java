package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.MetricsResponse;
import com.gateway.controlplane.dto.MetricsSnapshotRequest;
import com.gateway.controlplane.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Receives metrics from data-plane nodes and exposes aggregated metrics to the web-client.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Real-time metrics from gateway nodes")
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Receive metrics snapshot from a data-plane node.
     * This endpoint is called periodically (every 10 seconds) by each data-plane instance.
     */
    @PostMapping("/nodes/{nodeId}/metrics")
    @Operation(summary = "Record metrics from data-plane node", 
               description = "Receive and store metrics snapshot from a gateway node")
    public void recordMetrics(
            @Parameter(description = "Node ID (e.g., data-plane-abc123)")
            @PathVariable String nodeId,
            @RequestBody MetricsSnapshotRequest snapshot
    ) {
        metricsService.recordMetrics(snapshot);
    }

    /**
     * Get current metrics for a specific node.
     */
    @GetMapping("/nodes/{nodeId}/metrics")
    @Operation(summary = "Get node metrics", description = "Retrieve current metrics for a specific gateway node")
    public MetricsResponse getNodeMetrics(
            @Parameter(description = "Node ID")
            @PathVariable String nodeId
    ) {
        return metricsService.getNodeMetrics(nodeId)
                .orElse(new MetricsResponse(nodeId, 0, 0, 0, 0, 0, 0, 0));
    }

    /**
     * Get aggregated metrics for all active nodes.
     */
    @GetMapping("/metrics")
    @Operation(summary = "Get all metrics", description = "Retrieve aggregated metrics from all active gateway nodes")
    public List<MetricsResponse> getAllMetrics() {
        return metricsService.getAllMetrics();
    }

    /**
     * Get list of active node IDs.
     */
    @GetMapping("/metrics/nodes")
    @Operation(summary = "Get active nodes", description = "List all gateway nodes currently reporting metrics")
    public List<String> getActiveNodes() {
        return metricsService.getActiveNodes();
    }
}
