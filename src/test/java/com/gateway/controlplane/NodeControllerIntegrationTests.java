package com.gateway.controlplane;

import com.gateway.controlplane.entity.GatewayNode;
import com.gateway.controlplane.repository.GatewayNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NodeControllerIntegrationTests extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GatewayNodeRepository gatewayNodeRepository;

    @BeforeEach
    void setUp() {
        gatewayNodeRepository.deleteAll();
    }

    @Test
    void heartbeatUpsertsNodeAndMarksItHealthy() throws Exception {
        mockMvc.perform(post("/api/v1/nodes/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nodeId": "dummy-ms-01",
                                  "region": "local",
                                  "cpuUsage": 12,
                                  "memoryUsage": 25,
                                  "activeConnections": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId").value("dummy-ms-01"))
                .andExpect(jsonPath("$.status").value("ok"));

        mockMvc.perform(get("/api/v1/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nodeId").value("dummy-ms-01"))
                .andExpect(jsonPath("$[0].status").value("ok"));
    }

    @Test
    void listDerivesWarnAndErrorStatusesFromHeartbeatAge() throws Exception {
        gatewayNodeRepository.save(node("stale-warn", Instant.now().minusSeconds(45)));
        gatewayNodeRepository.save(node("stale-error", Instant.now().minusSeconds(120)));

        mockMvc.perform(get("/api/v1/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodesByStatus.warn").value(1))
                .andExpect(jsonPath("$.nodesByStatus.error").value(1));
    }

    private GatewayNode node(String nodeId, Instant lastHeartbeatAt) {
        GatewayNode node = new GatewayNode();
        node.setNodeId(nodeId);
        node.setRegion("local");
        node.setStatus("ok");
        node.setCpuUsage(0);
        node.setMemoryUsage(0);
        node.setActiveConnections(0);
        node.setLastHeartbeatAt(lastHeartbeatAt);
        return node;
    }
}
