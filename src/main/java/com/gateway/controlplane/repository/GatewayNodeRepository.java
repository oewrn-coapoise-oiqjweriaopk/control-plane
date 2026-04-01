package com.gateway.controlplane.repository;

import com.gateway.controlplane.entity.GatewayNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayNodeRepository extends JpaRepository<GatewayNode, Long> {
}
