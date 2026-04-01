package com.gateway.controlplane.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class RouteConfig extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false, unique = true)
    private String pathPattern;

    @Column(nullable = false)
    private String upstreamUrl;

    @Column(nullable = false)
    private boolean requiresAuth;

    @Column(nullable = false)
    private boolean cacheEnabled;

    @Column(nullable = false)
    private int rateLimitPerMinute;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int timeoutMillis;

    @Column(nullable = false)
    private String stripPrefix;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public String getUpstreamUrl() {
        return upstreamUrl;
    }

    public void setUpstreamUrl(String upstreamUrl) {
        this.upstreamUrl = upstreamUrl;
    }

    public boolean isRequiresAuth() {
        return requiresAuth;
    }

    public void setRequiresAuth(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public String getStripPrefix() {
        return stripPrefix;
    }

    public void setStripPrefix(String stripPrefix) {
        this.stripPrefix = stripPrefix;
    }
}
