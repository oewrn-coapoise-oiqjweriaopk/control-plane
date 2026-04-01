package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.PolicyRuleRequest;
import com.gateway.controlplane.entity.PolicyRule;
import com.gateway.controlplane.service.PolicyRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyRuleController {

    private final PolicyRuleService policyRuleService;

    public PolicyRuleController(PolicyRuleService policyRuleService) {
        this.policyRuleService = policyRuleService;
    }

    @GetMapping
    public List<PolicyRule> list() {
        return policyRuleService.findAll();
    }

    @GetMapping("/{id}")
    public PolicyRule get(@PathVariable Long id) {
        return policyRuleService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyRule create(@Valid @RequestBody PolicyRuleRequest request) {
        return policyRuleService.create(request);
    }

    @PutMapping("/{id}")
    public PolicyRule update(@PathVariable Long id, @Valid @RequestBody PolicyRuleRequest request) {
        return policyRuleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        policyRuleService.delete(id);
    }
}
