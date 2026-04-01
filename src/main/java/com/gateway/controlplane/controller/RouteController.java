package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.RouteRequest;
import com.gateway.controlplane.entity.RouteConfig;
import com.gateway.controlplane.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Routes", description = "Manage route definitions and upstream mappings for the gateway.")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    @Operation(summary = "List routes", description = "Returns all configured routes.")
    public List<RouteConfig> list() {
        return routeService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route", description = "Returns a single route by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route found"),
            @ApiResponse(responseCode = "404", description = "Route not found", content = @Content(schema = @Schema()))
    })
    public RouteConfig get(@PathVariable Long id) {
        return routeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create route", description = "Creates a new route and updates the Redis runtime projection.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Route created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema()))
    })
    public RouteConfig create(@Valid @RequestBody RouteRequest request) {
        return routeService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update route", description = "Updates an existing route and refreshes the runtime projection.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Route not found", content = @Content(schema = @Schema()))
    })
    public RouteConfig update(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        return routeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete route", description = "Deletes a route and removes it from the runtime projection.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Route deleted"),
            @ApiResponse(responseCode = "404", description = "Route not found", content = @Content(schema = @Schema()))
    })
    public void delete(@PathVariable Long id) {
        routeService.delete(id);
    }
}
