package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.OverviewResponse;
import com.gateway.controlplane.service.OverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/overview")
public class OverviewController {

    private final OverviewService overviewService;

    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping
    public OverviewResponse getOverview() {
        return overviewService.getOverview();
    }
}
