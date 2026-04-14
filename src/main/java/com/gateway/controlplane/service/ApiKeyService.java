package com.gateway.controlplane.service;

import com.gateway.controlplane.dto.ApiKeyCreateRequest;
import com.gateway.controlplane.dto.ApiKeyResponse;
import com.gateway.controlplane.dto.RuntimeApiKeyProjection;
import com.gateway.controlplane.entity.ApiKey;
import com.gateway.controlplane.exception.ResourceNotFoundException;
import com.gateway.controlplane.repository.ApiKeyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final RedisProjectionService redisProjectionService;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, RedisProjectionService redisProjectionService) {
        this.apiKeyRepository = apiKeyRepository;
        this.redisProjectionService = redisProjectionService;
    }

    public List<ApiKeyResponse> findAll() {
        return apiKeyRepository.findAll().stream()
                .map(apiKey -> toResponse(apiKey, null))
                .toList();
    }

    @Transactional
    public ApiKeyResponse create(ApiKeyCreateRequest request) {
        String rawKey = "gk_" + UUID.randomUUID().toString().replace("-", "");
        ApiKey apiKey = new ApiKey();
        apiKey.setName(request.name());
        apiKey.setOwner(request.owner());
        apiKey.setRateLimitPerMinute(request.rateLimitPerMinute());
        apiKey.setStatus("active");
        apiKey.setExpiresAt(request.expiresAt());
        apiKey.setKeyPrefix(rawKey.substring(0, 10));
        apiKey.setKeyHash(hash(rawKey));
        ApiKey saved = apiKeyRepository.save(apiKey);
        syncRedis();
        return toResponse(saved, rawKey);
    }

    @Transactional
    public ApiKeyResponse revoke(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API key " + id + " was not found"));
        apiKey.setStatus("revoked");
        ApiKey saved = apiKeyRepository.save(apiKey);
        syncRedis();
        return toResponse(saved, null);
    }

    public void syncRedis() {
        List<RuntimeApiKeyProjection> payload = apiKeyRepository.findAll().stream()
                .filter(apiKey -> "active".equalsIgnoreCase(apiKey.getStatus()))
                .map(this::toRuntimeProjection)
                .toList();
        redisProjectionService.publishApiKeys(payload);
    }

    private ApiKeyResponse toResponse(ApiKey apiKey, String plainTextKey) {
        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getKeyPrefix(),
                apiKey.getOwner(),
                apiKey.getStatus(),
                apiKey.getRateLimitPerMinute(),
                apiKey.getExpiresAt(),
                apiKey.getLastUsedAt(),
                apiKey.getCreatedAt(),
                plainTextKey
        );
    }

    private RuntimeApiKeyProjection toRuntimeProjection(ApiKey apiKey) {
        return new RuntimeApiKeyProjection(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getKeyPrefix(),
                apiKey.getKeyHash(),
                apiKey.getOwner(),
                apiKey.getStatus(),
                apiKey.getRateLimitPerMinute(),
                apiKey.getExpiresAt(),
                apiKey.getLastUsedAt(),
                apiKey.getCreatedAt()
        );
    }

    private String hash(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
