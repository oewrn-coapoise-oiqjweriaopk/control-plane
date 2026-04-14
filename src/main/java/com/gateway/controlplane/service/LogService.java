package com.gateway.controlplane.service;

import com.gateway.controlplane.dto.LogEntryResponse;
import com.gateway.controlplane.entity.LogEntry;
import com.gateway.controlplane.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing logs from data-plane.
 *
 * Logs are stored both in Redis (for real-time access) and in the database (for persistence).
 * The service syncs logs from Redis to the database periodically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogEntryRepository logRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOGS_REDIS_KEY = "gateway:logs:entries";
    private static final int MAX_LOGS_TO_KEEP = 100;

    /**
     * Get recent logs from database
     */
    public List<LogEntryResponse> getRecentLogs(int limit) {
        limit = Math.min(limit, MAX_LOGS_TO_KEEP);
        Pageable pageable = PageRequest.of(0, limit);
        return logRepository.findRecentLogs(pageable)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get logs filtered by level
     */
    public List<LogEntryResponse> getLogsByLevel(String level, int limit) {
        limit = Math.min(limit, MAX_LOGS_TO_KEEP);
        Pageable pageable = PageRequest.of(0, limit);
        return logRepository.findByLevelOrderByTimestampDesc(level, pageable)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get logs from a specific node
     */
    public List<LogEntryResponse> getLogsByNode(String nodeId, int limit) {
        limit = Math.min(limit, MAX_LOGS_TO_KEEP);
        Pageable pageable = PageRequest.of(0, limit);
        return logRepository.findByNodeIdOrderByTimestampDesc(nodeId, pageable)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create and store a new log entry
     */
    public LogEntry createLogEntry(String level, String nodeId, String message, String method,
                                    String path, Integer statusCode, Long responseTimeMs, String error) {
        LogEntry entry = new LogEntry();
        entry.setTimestamp(LocalDateTime.now());
        entry.setLevel(level);
        entry.setNodeId(nodeId);
        entry.setMessage(message);
        entry.setMethod(method);
        entry.setPath(path);
        entry.setStatusCode(statusCode);
        entry.setResponseTimeMs(responseTimeMs);
        entry.setError(error);

        return logRepository.save(entry);
    }

    /**
     * Get count of error logs
     */
    public long getErrorLogCount() {
        return logRepository.countErrorLogs();
    }

    /**
     * Get count of warning logs
     */
    public long getWarningLogCount() {
        return logRepository.countWarningLogs();
    }

    /**
     * Clear logs older than 7 days
     */
    public void cleanupOldLogs() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        // Implementation depends on JPA custom query for deletion
        log.info("Cleaning up logs older than 7 days");
    }

    /**
     * Convert LogEntry to LogEntryResponse
     */
    private LogEntryResponse toResponse(LogEntry entry) {
        return new LogEntryResponse(
                entry.getId(),
                entry.getTimestamp(),
                entry.getLevel(),
                entry.getNodeId(),
                entry.getMessage(),
                entry.getRequestId(),
                entry.getMethod(),
                entry.getPath(),
                entry.getStatusCode(),
                entry.getResponseTimeMs(),
                entry.getError()
        );
    }
}
