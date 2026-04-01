package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.PolicyRuleRequest;
import com.gateway.controlplane.entity.PolicyRule;
import com.gateway.controlplane.service.PolicyRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
@Tag(name = "Policies", description = "Manage runtime policy rules enforced by the gateway.")
public class PolicyRuleController {

    private final PolicyRuleService policyRuleService;

    public PolicyRuleController(PolicyRuleService policyRuleService) {
        this.policyRuleService = policyRuleService;
    }

    @GetMapping
    @Operation(summary = "List policy rules", description = "Returns every policy rule configured in the control plane.")
    public List<PolicyRule> list() {
        return policyRuleService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy rule", description = "Returns a single policy rule by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy rule found"),
            @ApiResponse(responseCode = "404", description = "Policy rule not found", content = @Content(schema = @Schema()))
    })
    public PolicyRule get(@PathVariable Long id) {
        return policyRuleService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create policy rule", description = "Creates a new policy rule and pushes the updated projection to Redis.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Policy rule created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema()))
    })
    public PolicyRule create(@Valid @RequestBody PolicyRuleRequest request) {
        return policyRuleService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update policy rule", description = "Updates an existing policy rule and refreshes the runtime projection.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy rule updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Policy rule not found", content = @Content(schema = @Schema()))
    })
    public PolicyRule update(@PathVariable Long id, @Valid @RequestBody PolicyRuleRequest request) {
        return policyRuleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete policy rule", description = "Deletes a policy rule and removes it from the Redis runtime projection.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Policy rule deleted"),
            @ApiResponse(responseCode = "404", description = "Policy rule not found", content = @Content(schema = @Schema()))
    })
    public void delete(@PathVariable Long id) {
        policyRuleService.delete(id);
    }
}
