package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.OverviewResponse;
import com.gateway.controlplane.service.OverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/overview")
@Tag(name = "Overview", description = "Read aggregate control-plane and runtime statistics.")
public class OverviewController {

    private final OverviewService overviewService;

    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping
    @Operation(summary = "Get overview snapshot", description = "Returns aggregate metrics for routes, policies, API keys, users, and nodes.")
    public OverviewResponse getOverview() {
        return overviewService.getOverview();
    }
}
