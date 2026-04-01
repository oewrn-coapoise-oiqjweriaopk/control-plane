package com.gateway.controlplane.repository;

import com.gateway.controlplane.entity.PolicyRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyRuleRepository extends JpaRepository<PolicyRule, Long> {
    List<PolicyRule> findAllByOrderByPriorityAscIdAsc();
}
