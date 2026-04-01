package com.gateway.controlplane;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.controlplane.dto.ApiKeyCreateRequest;
import com.gateway.controlplane.entity.ApiKey;
import com.gateway.controlplane.repository.ApiKeyRepository;
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

class ApiKeyControllerIntegrationTests extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @BeforeEach
    void setUp() {
        apiKeyRepository.deleteAll();
    }

    @Test
    void createApiKeyReturnsPlainTextKeyOnlyOnCreation() throws Exception {
        ApiKeyCreateRequest request = new ApiKeyCreateRequest(
                "frontend-dashboard",
                "platform-team",
                5000,
                Instant.parse("2026-12-31T00:00:00Z")
        );

        mockMvc.perform(post("/api/v1/api-keys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("frontend-dashboard"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.plainTextKey").isString())
                .andExpect(jsonPath("$.keyPrefix").isString());

        mockMvc.perform(get("/api/v1/api-keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plainTextKey").doesNotExist())
                .andExpect(jsonPath("$[0].status").value("active"));
    }

    @Test
    void revokeApiKeyMarksKeyAsRevoked() throws Exception {
        ApiKey apiKey = new ApiKey();
        apiKey.setName("ops-key");
        apiKey.setOwner("ops-team");
        apiKey.setKeyPrefix("gk_ops_01");
        apiKey.setKeyHash("hash-value");
        apiKey.setStatus("active");
        apiKey.setRateLimitPerMinute(1500);
        ApiKey saved = apiKeyRepository.save(apiKey);

        mockMvc.perform(post("/api/v1/api-keys/{id}/revoke", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("revoked"))
                .andExpect(jsonPath("$.plainTextKey").doesNotExist());
    }
}
