package com.gateway.controlplane.repository;

import com.gateway.controlplane.entity.GatewayNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface GatewayNodeRepository extends JpaRepository<GatewayNode, Long> {
    Optional<GatewayNode> findByNodeId(String nodeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM GatewayNode node WHERE node.lastHeartbeatAt IS NULL OR node.lastHeartbeatAt < :cutoff")
    int deleteStaleNodes(@Param("cutoff") Instant cutoff);
}
