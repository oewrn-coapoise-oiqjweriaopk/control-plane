package com.gateway.controlplane.controller;

import com.gateway.controlplane.entity.GatewayNode;
import com.gateway.controlplane.service.ReferenceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nodes")
@Tag(name = "Nodes", description = "Inspect gateway data-plane nodes and their health data.")
public class NodeController {

    private final ReferenceDataService referenceDataService;

    public NodeController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    @Operation(summary = "List gateway nodes", description = "Returns the current set of data-plane nodes known to the control plane.")
    public List<GatewayNode> list() {
        return referenceDataService.findNodes();
    }
}
