package com.gateway.controlplane.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PolicyRuleRequest(
        @NotBlank String name,
        @NotBlank String scope,
        @NotBlank String conditionExpression,
        @NotBlank String action,
        @Min(0) int priority,
        boolean enabled,
        @NotBlank String routePattern
) {
}
