package com.gateway.controlplane.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_entries")
public class LogEntry extends BaseEntity {

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 20)
    private String level; // INFO, WARN, ERROR, DEBUG

    @Column(nullable = false, length = 100)
    private String nodeId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(length = 100)
    private String requestId;

    @Column(length = 10)
    private String method; // GET, POST, PUT, DELETE, etc.

    @Column(length = 500)
    private String path;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(columnDefinition = "TEXT")
    private String error;

    // Constructors
    public LogEntry() {
    }

    public LogEntry(LocalDateTime timestamp, String level, String nodeId, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.nodeId = nodeId;
        this.message = message;
    }

    // Getters & Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
