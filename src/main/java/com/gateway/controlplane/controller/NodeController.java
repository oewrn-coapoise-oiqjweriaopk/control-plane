package com.gateway.controlplane.controller;

import com.gateway.controlplane.entity.GatewayNode;
import com.gateway.controlplane.service.ReferenceDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

    private final ReferenceDataService referenceDataService;

    public NodeController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<GatewayNode> list() {
        return referenceDataService.findNodes();
    }
}
