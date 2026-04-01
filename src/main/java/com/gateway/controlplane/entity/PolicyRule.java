package com.gateway.controlplane.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class PolicyRule extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String scope;

    @Column(nullable = false)
    private String conditionExpression;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private String routePattern;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRoutePattern() {
        return routePattern;
    }

    public void setRoutePattern(String routePattern) {
        this.routePattern = routePattern;
    }
}
