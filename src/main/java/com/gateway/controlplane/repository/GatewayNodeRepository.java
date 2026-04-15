package com.gateway.controlplane.repository;

import com.gateway.controlplane.entity.GatewayNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GatewayNodeRepository extends JpaRepository<GatewayNode, Long> {
    Optional<GatewayNode> findByNodeId(String nodeId);
}
