package com.gateway.controlplane;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.controlplane.dto.RouteRequest;
import com.gateway.controlplane.repository.RouteConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RouteControllerIntegrationTests extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RouteConfigRepository routeRepository;

    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();
    }

    @Test
    void createRouteReturnsPersistedRoute() throws Exception {
        RouteRequest request = new RouteRequest(
                "catalog-route",
                "GET",
                "/api/v2/catalog",
                "http://catalog-svc:8082",
                false,
                true,
                1200,
                "healthy",
                2500,
                "/api"
        );

        mockMvc.perform(post("/api/v1/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("catalog-route"))
                .andExpect(jsonPath("$.pathPattern").value("/api/v2/catalog"))
                .andExpect(jsonPath("$.cacheEnabled").value(true));

        mockMvc.perform(get("/api/v1/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("catalog-route"));
    }

    @Test
    void createRouteRejectsInvalidPayload() throws Exception {
        String invalidPayload = """
                {
                  "name": "",
                  "method": "GET",
                  "pathPattern": "/api/v2/catalog",
                  "upstreamUrl": "http://catalog-svc:8082",
                  "requiresAuth": false,
                  "cacheEnabled": true,
                  "rateLimitPerMinute": 100,
                  "status": "healthy",
                  "timeoutMillis": 2500,
                  "stripPrefix": "/api"
                }
                """;

        mockMvc.perform(post("/api/v1/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteMissingRouteReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/routes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Route 9999 was not found"));
    }
}
