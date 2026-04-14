package com.gateway.controlplane.listener;

import com.gateway.controlplane.service.ApiKeyService;
import com.gateway.controlplane.service.PolicyRuleService;
import com.gateway.controlplane.service.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Publishes initial runtime configuration to Redis on application startup.
 * This ensures the data plane can immediately load routes, policies, and API keys.
 */
@Component
public class RedisInitializationListener {

    private static final Logger log = LoggerFactory.getLogger(RedisInitializationListener.class);

    private final RouteService routeService;
    private final PolicyRuleService policyRuleService;
    private final ApiKeyService apiKeyService;

    public RedisInitializationListener(
            RouteService routeService,
            PolicyRuleService policyRuleService,
            ApiKeyService apiKeyService
    ) {
        this.routeService = routeService;
        this.policyRuleService = policyRuleService;
        this.apiKeyService = apiKeyService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Publishing initial runtime configuration to Redis");
        try {
            routeService.syncRedis();
            policyRuleService.syncRedis();
            apiKeyService.syncRedis();
            log.info("Successfully published routes, policies, and API keys to Redis");
        } catch (Exception e) {
            log.warn("Failed to publish initial configuration to Redis: {}", e.getMessage(), e);
        }
    }
}
