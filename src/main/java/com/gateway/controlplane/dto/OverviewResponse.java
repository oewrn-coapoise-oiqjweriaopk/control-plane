package com.gateway.controlplane.dto;

import java.util.Map;

public record OverviewResponse(
        long totalRoutes,
        long healthyRoutes,
        long activePolicies,
        long activeApiKeys,
        long onlineNodes,
        long totalAdmins,
        double averageNodeCpu,
        double averageNodeMemory,
        long totalNodeConnections,
        Map<String, Long> routesByStatus,
        Map<String, Long> nodesByStatus
) {
}
