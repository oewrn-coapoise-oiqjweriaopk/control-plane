package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.ApiKeyCreateRequest;
import com.gateway.controlplane.dto.ApiKeyResponse;
import com.gateway.controlplane.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @GetMapping
    public List<ApiKeyResponse> list() {
        return apiKeyService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyResponse create(@Valid @RequestBody ApiKeyCreateRequest request) {
        return apiKeyService.create(request);
    }

    @PostMapping("/{id}/revoke")
    public ApiKeyResponse revoke(@PathVariable Long id) {
        return apiKeyService.revoke(id);
    }
}
