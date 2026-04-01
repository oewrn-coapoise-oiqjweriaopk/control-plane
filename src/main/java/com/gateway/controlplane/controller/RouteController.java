package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.RouteRequest;
import com.gateway.controlplane.entity.RouteConfig;
import com.gateway.controlplane.service.RouteService;
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
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<RouteConfig> list() {
        return routeService.findAll();
    }

    @GetMapping("/{id}")
    public RouteConfig get(@PathVariable Long id) {
        return routeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RouteConfig create(@Valid @RequestBody RouteRequest request) {
        return routeService.create(request);
    }

    @PutMapping("/{id}")
    public RouteConfig update(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        return routeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        routeService.delete(id);
    }
}
