package com.gateway.controlplane.repository;

import com.gateway.controlplane.entity.LogEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    @Query("SELECT l FROM LogEntry l ORDER BY l.timestamp DESC")
    List<LogEntry> findRecentLogs(Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE l.level = :level ORDER BY l.timestamp DESC")
    List<LogEntry> findByLevelOrderByTimestampDesc(@Param("level") String level, Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE l.nodeId = :nodeId ORDER BY l.timestamp DESC")
    List<LogEntry> findByNodeIdOrderByTimestampDesc(@Param("nodeId") String nodeId, Pageable pageable);

    @Query("SELECT COUNT(l) FROM LogEntry l WHERE l.level = 'ERROR'")
    long countErrorLogs();

    @Query("SELECT COUNT(l) FROM LogEntry l WHERE l.level = 'WARN'")
    long countWarningLogs();
}
