package com.gateway.controlplane.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisProjectionService {

    private static final Logger log = LoggerFactory.getLogger(RedisProjectionService.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String routesKey;
    private final String policiesKey;
    private final String apiKeysKey;

    public RedisProjectionService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.redis.routes-key}") String routesKey,
            @Value("${app.redis.policies-key}") String policiesKey,
            @Value("${app.redis.api-keys-key}") String apiKeysKey
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.routesKey = routesKey;
        this.policiesKey = policiesKey;
        this.apiKeysKey = apiKeysKey;
    }

    public void publishRoutes(List<?> routes) {
        writeJson(routesKey, routes);
    }

    public void publishPolicies(List<?> policies) {
        writeJson(policiesKey, policies);
    }

    public void publishApiKeys(List<?> apiKeys) {
        writeJson(apiKeysKey, apiKeys);
    }

    private void writeJson(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to publish config to Redis for key " + key, exception);
        } catch (Exception exception) {
            log.warn("Skipping Redis projection for key {} because Redis is unavailable: {}", key, exception.getMessage());
        }
    }
}
