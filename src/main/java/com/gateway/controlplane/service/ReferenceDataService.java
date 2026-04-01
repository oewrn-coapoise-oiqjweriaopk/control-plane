package com.gateway.controlplane.service;

import com.gateway.controlplane.entity.AdminUser;
import com.gateway.controlplane.entity.GatewayNode;
import com.gateway.controlplane.repository.AdminUserRepository;
import com.gateway.controlplane.repository.GatewayNodeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReferenceDataService {

    private final AdminUserRepository adminUserRepository;
    private final GatewayNodeRepository gatewayNodeRepository;

    public ReferenceDataService(AdminUserRepository adminUserRepository, GatewayNodeRepository gatewayNodeRepository) {
        this.adminUserRepository = adminUserRepository;
        this.gatewayNodeRepository = gatewayNodeRepository;
    }

    public List<AdminUser> findUsers() {
        return adminUserRepository.findAll();
    }

    public List<GatewayNode> findNodes() {
        return gatewayNodeRepository.findAll();
    }
}
