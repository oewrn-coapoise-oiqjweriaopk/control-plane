package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.NodeHeartbeatRequest;
import com.gateway.controlplane.entity.GatewayNode;
import com.gateway.controlplane.service.NodeHeartbeatService;
import com.gateway.controlplane.service.ReferenceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nodes")
@Tag(name = "Nodes", description = "Inspect gateway data-plane nodes and their health data.")
public class NodeController {

    private final ReferenceDataService referenceDataService;
    private final NodeHeartbeatService nodeHeartbeatService;

    public NodeController(
            ReferenceDataService referenceDataService,
            NodeHeartbeatService nodeHeartbeatService
    ) {
        this.referenceDataService = referenceDataService;
        this.nodeHeartbeatService = nodeHeartbeatService;
    }

    @GetMapping
    @Operation(summary = "List gateway nodes", description = "Returns the current set of data-plane nodes known to the control plane.")
    public List<GatewayNode> list() {
        return referenceDataService.findNodes();
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "Record service heartbeat", description = "Upserts node heartbeat information and refreshes health status.")
    public GatewayNode heartbeat(@Valid @RequestBody NodeHeartbeatRequest request) {
        return nodeHeartbeatService.recordHeartbeat(request);
    }
}
