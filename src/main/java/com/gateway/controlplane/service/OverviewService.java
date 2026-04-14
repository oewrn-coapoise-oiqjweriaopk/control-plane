package com.gateway.controlplane.service;

import com.gateway.controlplane.dto.OverviewResponse;
import com.gateway.controlplane.entity.GatewayNode;
import com.gateway.controlplane.entity.RouteConfig;
import com.gateway.controlplane.repository.AdminUserRepository;
import com.gateway.controlplane.repository.ApiKeyRepository;
import com.gateway.controlplane.repository.GatewayNodeRepository;
import com.gateway.controlplane.repository.PolicyRuleRepository;
import com.gateway.controlplane.repository.RouteConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OverviewService {

    private final RouteConfigRepository routeRepository;
    private final PolicyRuleRepository policyRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final GatewayNodeRepository gatewayNodeRepository;
    private final AdminUserRepository adminUserRepository;
    private final LogService logService;

    public OverviewService(
            RouteConfigRepository routeRepository,
            PolicyRuleRepository policyRepository,
            ApiKeyRepository apiKeyRepository,
            GatewayNodeRepository gatewayNodeRepository,
            AdminUserRepository adminUserRepository,
            LogService logService
    ) {
        this.routeRepository = routeRepository;
        this.policyRepository = policyRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.gatewayNodeRepository = gatewayNodeRepository;
        this.adminUserRepository = adminUserRepository;
        this.logService = logService;
    }

    public OverviewResponse getOverview() {
        List<RouteConfig> routes = routeRepository.findAll();
        List<GatewayNode> nodes = gatewayNodeRepository.findAll();

        long healthyRoutes = routes.stream()
                .filter(route -> "healthy".equalsIgnoreCase(route.getStatus()))
                .count();
        long activePolicies = policyRepository.findAllByOrderByPriorityAscIdAsc().stream()
                .filter(policy -> policy.isEnabled())
                .count();
        long activeApiKeys = apiKeyRepository.findAll().stream()
                .filter(apiKey -> "active".equalsIgnoreCase(apiKey.getStatus()))
                .count();
        long onlineNodes = nodes.stream()
                .filter(node -> "ok".equalsIgnoreCase(node.getStatus()) || "healthy".equalsIgnoreCase(node.getStatus()))
                .count();

        double averageCpu = nodes.stream().mapToInt(GatewayNode::getCpuUsage).average().orElse(0);
        double averageMemory = nodes.stream().mapToInt(GatewayNode::getMemoryUsage).average().orElse(0);
        long totalConnections = nodes.stream().mapToLong(GatewayNode::getActiveConnections).sum();

        // Get recent logs (up to 50)
        var recentLogs = logService.getRecentLogs(50);

        return new OverviewResponse(
                routes.size(),
                healthyRoutes,
                activePolicies,
                activeApiKeys,
                onlineNodes,
                adminUserRepository.count(),
                averageCpu,
                averageMemory,
                totalConnections,
                routes.stream().collect(Collectors.groupingBy(RouteConfig::getStatus, Collectors.counting())),
                nodes.stream().collect(Collectors.groupingBy(GatewayNode::getStatus, Collectors.counting())),
                recentLogs
        );
    }
}

