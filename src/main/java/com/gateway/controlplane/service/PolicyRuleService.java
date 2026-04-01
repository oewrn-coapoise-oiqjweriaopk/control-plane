package com.gateway.controlplane.service;

import com.gateway.controlplane.dto.PolicyRuleRequest;
import com.gateway.controlplane.entity.PolicyRule;
import com.gateway.controlplane.exception.ResourceNotFoundException;
import com.gateway.controlplane.repository.PolicyRuleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyRuleService {

    private final PolicyRuleRepository policyRepository;
    private final RedisProjectionService redisProjectionService;

    public PolicyRuleService(PolicyRuleRepository policyRepository, RedisProjectionService redisProjectionService) {
        this.policyRepository = policyRepository;
        this.redisProjectionService = redisProjectionService;
    }

    public List<PolicyRule> findAll() {
        return policyRepository.findAllByOrderByPriorityAscIdAsc();
    }

    public PolicyRule findById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy " + id + " was not found"));
    }

    @Transactional
    public PolicyRule create(PolicyRuleRequest request) {
        PolicyRule policyRule = new PolicyRule();
        apply(policyRule, request);
        PolicyRule saved = policyRepository.save(policyRule);
        syncRedis();
        return saved;
    }

    @Transactional
    public PolicyRule update(Long id, PolicyRuleRequest request) {
        PolicyRule policyRule = findById(id);
        apply(policyRule, request);
        PolicyRule saved = policyRepository.save(policyRule);
        syncRedis();
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        PolicyRule policyRule = findById(id);
        policyRepository.delete(policyRule);
        syncRedis();
    }

    public void syncRedis() {
        redisProjectionService.publishPolicies(policyRepository.findAllByOrderByPriorityAscIdAsc());
    }

    private void apply(PolicyRule policyRule, PolicyRuleRequest request) {
        policyRule.setName(request.name());
        policyRule.setScope(request.scope());
        policyRule.setConditionExpression(request.conditionExpression());
        policyRule.setAction(request.action());
        policyRule.setPriority(request.priority());
        policyRule.setEnabled(request.enabled());
        policyRule.setRoutePattern(request.routePattern());
    }
}
