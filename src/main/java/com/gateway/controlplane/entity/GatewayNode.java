package com.gateway.controlplane.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.Instant;

@Entity
public class GatewayNode extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String nodeId;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int cpuUsage;

    @Column(nullable = false)
    private int memoryUsage;

    @Column(nullable = false)
    private long activeConnections;

    @Column(nullable = false)
    private Instant lastHeartbeatAt;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(int cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public int getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(int memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public long getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(long activeConnections) {
        this.activeConnections = activeConnections;
    }

    public Instant getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(Instant lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }
}
