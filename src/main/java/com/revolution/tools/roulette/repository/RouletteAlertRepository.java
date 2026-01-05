package com.revolution.tools.roulette.repository;

import com.revolution.tools.roulette.entity.RouletteAlert;
import com.revolution.tools.roulette.enums.AlertSeverity;
import com.revolution.tools.roulette.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RouletteAlert entities.
 */
@Repository
public interface RouletteAlertRepository extends JpaRepository<RouletteAlert, Long> {

    /**
     * Finds all alerts for a session.
     */
    List<RouletteAlert> findBySessionIdOrderByTimestampDesc(Long sessionId);

    /**
     * Finds unacknowledged alerts for a session.
     */
    List<RouletteAlert> findBySessionIdAndAcknowledged(Long sessionId, Boolean acknowledged);

    /**
     * Finds unacknowledged alerts for a session, ordered by timestamp.
     */
    List<RouletteAlert> findBySessionIdAndAcknowledgedFalseOrderByTimestampDesc(Long sessionId);

    /**
     * Finds critical alerts for a session.
     */
    List<RouletteAlert> findBySessionIdAndSeverity(Long sessionId, AlertSeverity severity);

    /**
     * Finds unacknowledged alerts by session and severity.
     */
    List<RouletteAlert> findBySessionIdAndSeverityAndAcknowledgedFalse(Long sessionId, AlertSeverity severity);

    /**
     * Finds unacknowledged critical alerts for a session.
     */
    @Query("SELECT a FROM RouletteAlert a WHERE a.sessionId = :sessionId AND a.severity = 'CRITICAL' AND a.acknowledged = false ORDER BY a.timestamp DESC")
    List<RouletteAlert> findActiveCriticalAlerts(@Param("sessionId") Long sessionId);

    /**
     * Counts unacknowledged alerts for a session.
     */
    Long countBySessionIdAndAcknowledged(Long sessionId, Boolean acknowledged);

    /**
     * Finds alerts of a specific type for a session.
     */
    List<RouletteAlert> findBySessionIdAndAlertType(Long sessionId, AlertType alertType);

    /**
     * Deletes all alerts for a session.
     */
    void deleteBySessionId(Long sessionId);
}
