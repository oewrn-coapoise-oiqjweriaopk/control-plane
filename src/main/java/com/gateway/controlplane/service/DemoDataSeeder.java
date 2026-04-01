package com.gateway.controlplane.service;

import com.gateway.controlplane.dto.ApiKeyCreateRequest;
import com.gateway.controlplane.dto.PolicyRuleRequest;
import com.gateway.controlplane.dto.RouteRequest;
import com.gateway.controlplane.entity.AdminUser;
import com.gateway.controlplane.entity.GatewayNode;
import com.gateway.controlplane.repository.AdminUserRepository;
import com.gateway.controlplane.repository.GatewayNodeRepository;
import com.gateway.controlplane.repository.RouteConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final boolean seedDemoData;
    private final RouteConfigRepository routeRepository;
    private final AdminUserRepository adminUserRepository;
    private final GatewayNodeRepository gatewayNodeRepository;
    private final RouteService routeService;
    private final PolicyRuleService policyRuleService;
    private final ApiKeyService apiKeyService;

    public DemoDataSeeder(
            @Value("${app.seed-demo-data:true}") boolean seedDemoData,
            RouteConfigRepository routeRepository,
            AdminUserRepository adminUserRepository,
            GatewayNodeRepository gatewayNodeRepository,
            RouteService routeService,
            PolicyRuleService policyRuleService,
            ApiKeyService apiKeyService
    ) {
        this.seedDemoData = seedDemoData;
        this.routeRepository = routeRepository;
        this.adminUserRepository = adminUserRepository;
        this.gatewayNodeRepository = gatewayNodeRepository;
        this.routeService = routeService;
        this.policyRuleService = policyRuleService;
        this.apiKeyService = apiKeyService;
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData || routeRepository.count() > 0) {
            return;
        }

        routeService.create(new RouteRequest("users-by-id", "GET", "/api/v2/users/{id}", "http://user-svc:8080", true, true, 1200, "healthy", 3000, "/api"));
        routeService.create(new RouteRequest("auth-token", "POST", "/api/v2/auth/token", "http://auth-svc:8081", false, false, 500, "healthy", 2500, "/api"));
        routeService.create(new RouteRequest("products", "GET", "/api/v2/products", "http://catalog-svc:8082", false, true, 2000, "healthy", 2500, "/api"));
        routeService.create(new RouteRequest("orders", "PUT", "/api/v2/orders/{id}", "http://order-svc:8083", true, false, 400, "degraded", 4000, "/api"));

        policyRuleService.create(new PolicyRuleRequest("tenant-rate-limit", "tenant", "requestsPerMinute > route.rateLimit", "throttle", 10, true, "/api/v2/**"));
        policyRuleService.create(new PolicyRuleRequest("admin-write-guard", "principal", "role != 'admin' && method in ['POST','PUT','DELETE']", "deny", 20, true, "/api/v2/config/**"));
        policyRuleService.create(new PolicyRuleRequest("region-block", "request", "geo.country in ['KP','IR']", "deny", 30, true, "/api/v2/**"));

        apiKeyService.create(new ApiKeyCreateRequest("frontend-dashboard", "platform-team", 5000, Instant.now().plusSeconds(60L * 60 * 24 * 90)));
        apiKeyService.create(new ApiKeyCreateRequest("ops-automation", "ops-team", 2500, Instant.now().plusSeconds(60L * 60 * 24 * 30)));

        seedUsers();
        seedNodes();
    }

    private void seedUsers() {
        adminUserRepository.save(buildUser("Priya Mehta", "p.mehta@corp.io", "admin", true, "active", Instant.now().minusSeconds(120)));
        adminUserRepository.save(buildUser("Jordan Cole", "j.cole@corp.io", "ops", true, "active", Instant.now().minusSeconds(840)));
        adminUserRepository.save(buildUser("Alex Chen", "a.chen@corp.io", "readonly", true, "active", Instant.now().minusSeconds(7200)));
    }

    private void seedNodes() {
        gatewayNodeRepository.save(buildNode("gw-01", "us-east-1a", "ok", 34, 61, 1420));
        gatewayNodeRepository.save(buildNode("gw-02", "us-east-1b", "ok", 41, 58, 1380));
        gatewayNodeRepository.save(buildNode("gw-03", "us-west-2a", "ok", 28, 52, 960));
        gatewayNodeRepository.save(buildNode("gw-04", "eu-west-1a", "warn", 67, 74, 2100));
    }

    private AdminUser buildUser(String name, String email, String role, boolean mfaEnabled, String status, Instant lastSeenAt) {
        AdminUser user = new AdminUser();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setMfaEnabled(mfaEnabled);
        user.setStatus(status);
        user.setLastSeenAt(lastSeenAt);
        return user;
    }

    private GatewayNode buildNode(String nodeId, String region, String status, int cpu, int memory, long connections) {
        GatewayNode node = new GatewayNode();
        node.setNodeId(nodeId);
        node.setRegion(region);
        node.setStatus(status);
        node.setCpuUsage(cpu);
        node.setMemoryUsage(memory);
        node.setActiveConnections(connections);
        node.setLastHeartbeatAt(Instant.now().minusSeconds(15));
        return node;
    }
}
