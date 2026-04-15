package com.gateway.controlplane.controller;

import com.gateway.controlplane.dto.LogEntryResponse;
import com.gateway.controlplane.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/logs")
@Tag(name = "Logs", description = "Access and query logs from the data-plane.")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Receive a log entry from data-plane.
     * This is called asynchronously when data-plane emits logs.
     */
    @PostMapping
    @Operation(summary = "Record log entry from data-plane", description = "Receive and store a log entry from gateway node")
    public void recordLog(@RequestBody LogEntryRequest logRequest) {
        logService.createLogEntry(
                logRequest.level(),
                logRequest.nodeId(),
                logRequest.message(),
                logRequest.method(),
                logRequest.path(),
                logRequest.statusCode(),
                logRequest.responseTimeMs(),
                logRequest.error()
        );
    }

    @GetMapping
    @Operation(summary = "Get recent logs", description = "Retrieve recent logs from data-plane. Default limit is 50.")
    public List<LogEntryResponse> getRecentLogs(
            @Parameter(description = "Number of logs to retrieve (max 100)", example = "50")
            @RequestParam(defaultValue = "50") int limit
    ) {
        return logService.getRecentLogs(limit);
    }

    @GetMapping("/level/{level}")
    @Operation(summary = "Get logs by level", description = "Filter logs by level (INFO, WARN, ERROR, DEBUG)")
    public List<LogEntryResponse> getLogsByLevel(
            @Parameter(description = "Log level", example = "ERROR")
            @PathVariable String level,
            @Parameter(description = "Number of logs to retrieve", example = "50")
            @RequestParam(defaultValue = "50") int limit
    ) {
        return logService.getLogsByLevel(level.toUpperCase(), limit);
    }

    @GetMapping("/node/{nodeId}")
    @Operation(summary = "Get logs by node", description = "Retrieve logs from a specific gateway node")
    public List<LogEntryResponse> getLogsByNode(
            @Parameter(description = "Node ID (e.g., gw-01)", example = "gw-01")
            @PathVariable String nodeId,
            @Parameter(description = "Number of logs to retrieve", example = "50")
            @RequestParam(defaultValue = "50") int limit
    ) {
        return logService.getLogsByNode(nodeId, limit);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get log statistics", description = "Get counts of error and warning logs")
    public Map<String, Object> getLogStats() {
        return Map.of(
                "errorCount", logService.getErrorLogCount(),
                "warningCount", logService.getWarningLogCount()
        );
    }

    /**
     * DTO for receiving log entries from data-plane.
     */
    public record LogEntryRequest(
            String level,
            String nodeId,
            String message,
            String method,
            String path,
            Integer statusCode,
            Long responseTimeMs,
            String error
    ) {
    }
}
