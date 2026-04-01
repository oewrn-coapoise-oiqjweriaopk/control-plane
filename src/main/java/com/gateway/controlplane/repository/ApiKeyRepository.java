package com.gateway.controlplane.repository;

import com.gateway.controlplane.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
}
