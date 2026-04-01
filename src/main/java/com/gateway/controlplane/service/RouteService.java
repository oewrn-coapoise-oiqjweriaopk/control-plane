package com.gateway.controlplane.service;

import com.gateway.controlplane.dto.RouteRequest;
import com.gateway.controlplane.entity.RouteConfig;
import com.gateway.controlplane.exception.ResourceNotFoundException;
import com.gateway.controlplane.repository.RouteConfigRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private final RouteConfigRepository routeRepository;
    private final RedisProjectionService redisProjectionService;

    public RouteService(RouteConfigRepository routeRepository, RedisProjectionService redisProjectionService) {
        this.routeRepository = routeRepository;
        this.redisProjectionService = redisProjectionService;
    }

    public List<RouteConfig> findAll() {
        return routeRepository.findAll();
    }

    public RouteConfig findById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route " + id + " was not found"));
    }

    @Transactional
    public RouteConfig create(RouteRequest request) {
        RouteConfig route = new RouteConfig();
        apply(route, request);
        RouteConfig saved = routeRepository.save(route);
        syncRedis();
        return saved;
    }

    @Transactional
    public RouteConfig update(Long id, RouteRequest request) {
        RouteConfig route = findById(id);
        apply(route, request);
        RouteConfig saved = routeRepository.save(route);
        syncRedis();
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        RouteConfig route = findById(id);
        routeRepository.delete(route);
        syncRedis();
    }

    public void syncRedis() {
        redisProjectionService.publishRoutes(routeRepository.findAll());
    }

    private void apply(RouteConfig route, RouteRequest request) {
        route.setName(request.name());
        route.setMethod(request.method());
        route.setPathPattern(request.pathPattern());
        route.setUpstreamUrl(request.upstreamUrl());
        route.setRequiresAuth(request.requiresAuth());
        route.setCacheEnabled(request.cacheEnabled());
        route.setRateLimitPerMinute(request.rateLimitPerMinute());
        route.setStatus(request.status());
        route.setTimeoutMillis(request.timeoutMillis());
        route.setStripPrefix(request.stripPrefix());
    }
}
