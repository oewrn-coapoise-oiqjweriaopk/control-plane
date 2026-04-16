package com.gateway.controlplane.service;

import com.gateway.controlplane.dto.MetricsSnapshotRequest;
import com.gateway.controlplane.dto.NodeHeartbeatRequest;
import com.gateway.controlplane.entity.GatewayNode;
import com.gateway.controlplane.repository.GatewayNodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class NodeHeartbeatService {

    private final GatewayNodeRepository gatewayNodeRepository;
    private final Duration warnAfter;
    private final Duration errorAfter;
    private final Duration pruneAfter;

    public NodeHeartbeatService(
            GatewayNodeRepository gatewayNodeRepository,
            @Value("${app.heartbeat.warn-after-seconds:30}") long warnAfterSeconds,
            @Value("${app.heartbeat.error-after-seconds:90}") long errorAfterSeconds,
            @Value("${app.heartbeat.prune-after-seconds:300}") long pruneAfterSeconds
    ) {
        this.gatewayNodeRepository = gatewayNodeRepository;
        this.warnAfter = Duration.ofSeconds(Math.max(1, warnAfterSeconds));
        this.errorAfter = Duration.ofSeconds(Math.max(1, errorAfterSeconds));
        this.pruneAfter = Duration.ofSeconds(Math.max(1, pruneAfterSeconds));
    }

    public GatewayNode recordHeartbeat(NodeHeartbeatRequest request) {
        GatewayNode node = gatewayNodeRepository.findByNodeId(request.nodeId())
                .orElseGet(GatewayNode::new);

        if (node.getNodeId() == null) {
            node.setNodeId(request.nodeId());
            node.setRegion(defaultRegion(request.region()));
            node.setMemoryUsage(0);
            node.setCpuUsage(0);
            node.setActiveConnections(0);
        } else if (request.region() != null && !request.region().isBlank()) {
            node.setRegion(request.region().trim());
        }

        if (request.cpuUsage() != null) {
            node.setCpuUsage(clampPercent(request.cpuUsage()));
        }
        if (request.memoryUsage() != null) {
            node.setMemoryUsage(clampPercent(request.memoryUsage()));
        }
        if (request.activeConnections() != null) {
            node.setActiveConnections(Math.max(0, request.activeConnections()));
        }

        node.setLastHeartbeatAt(Instant.now());
        node.setStatus("ok");

        return gatewayNodeRepository.save(node);
    }

    public void recordHeartbeatFromMetrics(MetricsSnapshotRequest snapshot) {
        GatewayNode node = gatewayNodeRepository.findByNodeId(snapshot.nodeId())
                .orElseGet(GatewayNode::new);

        if (node.getNodeId() == null) {
            node.setNodeId(snapshot.nodeId());
            node.setRegion("runtime");
            node.setMemoryUsage(0);
            node.setActiveConnections(0);
        }

        node.setCpuUsage(clampPercent((int) Math.round(snapshot.cpuUsagePercent())));
        node.setLastHeartbeatAt(Instant.now());
        node.setStatus("ok");
        gatewayNodeRepository.save(node);
    }

    public List<GatewayNode> enrichNodesWithComputedStatus(List<GatewayNode> nodes) {
        Instant now = Instant.now();
        for (GatewayNode node : nodes) {
            node.setStatus(computeStatus(node.getLastHeartbeatAt(), now));
        }
        return nodes;
    }

    public int pruneStaleNodes() {
        Instant cutoff = Instant.now().minus(pruneAfter);
        return gatewayNodeRepository.deleteStaleNodes(cutoff);
    }

    public String computeStatus(Instant lastHeartbeatAt) {
        return computeStatus(lastHeartbeatAt, Instant.now());
    }

    private String computeStatus(Instant lastHeartbeatAt, Instant now) {
        if (lastHeartbeatAt == null) {
            return "error";
        }

        Duration age = Duration.between(lastHeartbeatAt, now);
        if (age.compareTo(errorAfter) >= 0) {
            return "error";
        }
        if (age.compareTo(warnAfter) >= 0) {
            return "warn";
        }
        return "ok";
    }

    private int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String defaultRegion(String region) {
        if (region == null || region.isBlank()) {
            return "unknown";
        }
        return region.trim();
    }
}
