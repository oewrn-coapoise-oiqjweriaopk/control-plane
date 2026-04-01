package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.ApiKeyCreateRequest;
import com.gateway.controlplane.dto.ApiKeyResponse;
import com.gateway.controlplane.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "API Keys", description = "Manage gateway API keys used by clients and tenants.")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @GetMapping
    @Operation(summary = "List API keys", description = "Returns all API keys registered in the control plane.")
    public List<ApiKeyResponse> list() {
        return apiKeyService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create API key", description = "Creates a new API key and returns the plaintext key once.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "API key created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema()))
    })
    public ApiKeyResponse create(@Valid @RequestBody ApiKeyCreateRequest request) {
        return apiKeyService.create(request);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Revoke API key", description = "Revokes an API key so it can no longer be used by the data plane.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "API key revoked"),
            @ApiResponse(responseCode = "404", description = "API key not found", content = @Content(schema = @Schema()))
    })
    public ApiKeyResponse revoke(@PathVariable Long id) {
        return apiKeyService.revoke(id);
    }
}
