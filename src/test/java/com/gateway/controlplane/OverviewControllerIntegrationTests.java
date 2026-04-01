package com.gateway.controlplane;

import com.gateway.controlplane.entity.AdminUser;
import com.gateway.controlplane.entity.ApiKey;
import com.gateway.controlplane.entity.GatewayNode;
import com.gateway.controlplane.entity.PolicyRule;
import com.gateway.controlplane.entity.RouteConfig;
import com.gateway.controlplane.repository.AdminUserRepository;
import com.gateway.controlplane.repository.ApiKeyRepository;
import com.gateway.controlplane.repository.GatewayNodeRepository;
import com.gateway.controlplane.repository.PolicyRuleRepository;
import com.gateway.controlplane.repository.RouteConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OverviewControllerIntegrationTests extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RouteConfigRepository routeRepository;

    @Autowired
    private PolicyRuleRepository policyRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private GatewayNodeRepository nodeRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @BeforeEach
    void setUp() {
        adminUserRepository.deleteAll();
        nodeRepository.deleteAll();
        apiKeyRepository.deleteAll();
        policyRepository.deleteAll();
        routeRepository.deleteAll();
    }

    @Test
    void overviewAggregatesPersistedControlPlaneState() throws Exception {
        routeRepository.save(route("users", "healthy"));
        routeRepository.save(route("orders", "degraded"));

        policyRepository.save(policy("tenant-limit", true, 10));
        policyRepository.save(policy("country-block", false, 20));

        apiKeyRepository.save(apiKey("dashboard-key", "active"));
        apiKeyRepository.save(apiKey("legacy-key", "revoked"));

        nodeRepository.save(node("gw-01", "ok", 32, 55, 1200));
        nodeRepository.save(node("gw-02", "warn", 67, 71, 2100));

        adminUserRepository.save(user("Priya Mehta", "p.mehta@corp.io"));

        mockMvc.perform(get("/api/v1/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRoutes").value(2))
                .andExpect(jsonPath("$.healthyRoutes").value(1))
                .andExpect(jsonPath("$.activePolicies").value(1))
                .andExpect(jsonPath("$.activeApiKeys").value(1))
                .andExpect(jsonPath("$.onlineNodes").value(1))
                .andExpect(jsonPath("$.totalAdmins").value(1))
                .andExpect(jsonPath("$.routesByStatus.healthy").value(1))
                .andExpect(jsonPath("$.routesByStatus.degraded").value(1))
                .andExpect(jsonPath("$.nodesByStatus.ok").value(1))
                .andExpect(jsonPath("$.nodesByStatus.warn").value(1));
    }

    private RouteConfig route(String name, String status) {
        RouteConfig route = new RouteConfig();
        route.setName(name);
        route.setMethod("GET");
        route.setPathPattern("/api/v2/" + name);
        route.setUpstreamUrl("http://" + name + "-svc:8080");
        route.setRequiresAuth(true);
        route.setCacheEnabled(false);
        route.setRateLimitPerMinute(1000);
        route.setStatus(status);
        route.setTimeoutMillis(2500);
        route.setStripPrefix("/api");
        return route;
    }

    private PolicyRule policy(String name, boolean enabled, int priority) {
        PolicyRule policy = new PolicyRule();
        policy.setName(name);
        policy.setScope("tenant");
        policy.setConditionExpression("requestsPerMinute > limit");
        policy.setAction("throttle");
        policy.setPriority(priority);
        policy.setEnabled(enabled);
        policy.setRoutePattern("/api/v2/**");
        return policy;
    }

    private ApiKey apiKey(String name, String status) {
        ApiKey apiKey = new ApiKey();
        apiKey.setName(name);
        apiKey.setOwner("platform-team");
        apiKey.setKeyPrefix(name.substring(0, Math.min(10, name.length())));
        apiKey.setKeyHash(name + "-hash");
        apiKey.setStatus(status);
        apiKey.setRateLimitPerMinute(2000);
        return apiKey;
    }

    private GatewayNode node(String nodeId, String status, int cpu, int memory, long connections) {
        GatewayNode node = new GatewayNode();
        node.setNodeId(nodeId);
        node.setRegion("us-east-1a");
        node.setStatus(status);
        node.setCpuUsage(cpu);
        node.setMemoryUsage(memory);
        node.setActiveConnections(connections);
        node.setLastHeartbeatAt(Instant.parse("2026-04-01T00:00:00Z"));
        return node;
    }

    private AdminUser user(String name, String email) {
        AdminUser user = new AdminUser();
        user.setName(name);
        user.setEmail(email);
        user.setRole("admin");
        user.setMfaEnabled(true);
        user.setStatus("active");
        user.setLastSeenAt(Instant.parse("2026-04-01T00:00:00Z"));
        return user;
    }
}
