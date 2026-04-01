package com.gateway.controlplane.repository;

import com.gateway.controlplane.entity.RouteConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteConfigRepository extends JpaRepository<RouteConfig, Long> {
}
