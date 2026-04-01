package com.gateway.controlplane.repository;

import com.gateway.controlplane.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
}
