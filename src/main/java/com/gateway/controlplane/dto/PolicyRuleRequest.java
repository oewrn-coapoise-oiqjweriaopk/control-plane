package com.gateway.controlplane.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for creating or updating a policy rule.")
public record PolicyRuleRequest(
        @Schema(description = "Policy name.", example = "tenant-rate-limit")
        @NotBlank String name,
        @Schema(description = "Policy scope such as tenant, auth, or cache.", example = "tenant")
        @NotBlank String scope,
        @Schema(description = "Condition expression evaluated by the data plane.", example = "requestsPerMinute > 100")
        @NotBlank String conditionExpression,
        @Schema(description = "Action to take when the condition matches.", example = "throttle")
        @NotBlank String action,
        @Schema(description = "Evaluation priority, lower or higher depending on runtime semantics.", example = "10")
        @Min(0) int priority,
        @Schema(description = "Whether the rule is currently enabled.", example = "true")
        boolean enabled,
        @Schema(description = "Route pattern targeted by this policy.", example = "/api/v1/demo")
        @NotBlank String routePattern
) {
}
