package com.revolution.tools.roulette.repository;

import com.revolution.tools.roulette.entity.RouletteSession;
import com.revolution.tools.roulette.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RouletteSession entities.
 */
@Repository
public interface RouletteSessionRepository extends JpaRepository<RouletteSession, Long> {

    /**
     * Finds a session by its session ID.
     */
    Optional<RouletteSession> findBySessionId(String sessionId);

    /**
     * Finds all sessions with a specific status.
     */
    List<RouletteSession> findByStatus(SessionStatus status);

    /**
     * Finds the currently active session (if any).
     */
    Optional<RouletteSession> findFirstByStatusOrderByStartTimeDesc(SessionStatus status);

    /**
     * Finds all sessions that ended in profit.
     */
    @Query("SELECT s FROM RouletteSession s WHERE s.currentProfit > 0 AND s.status IN ('COMPLETED', 'STOPPED')")
    List<RouletteSession> findProfitableSessions();

    /**
     * Finds all sessions that ended in loss.
     */
    @Query("SELECT s FROM RouletteSession s WHERE s.currentProfit < 0 AND s.status IN ('COMPLETED', 'STOPPED')")
    List<RouletteSession> findLosSessions();

    /**
     * Finds sessions within a date range.
     */
    List<RouletteSession> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds recent sessions (last N days).
     */
    @Query("SELECT s FROM RouletteSession s WHERE s.startTime >= :since ORDER BY s.startTime DESC")
    List<RouletteSession> findRecentSessions(@Param("since") LocalDateTime since);

    /**
     * Counts sessions that hit stop-loss.
     */
    @Query("SELECT COUNT(s) FROM RouletteSession s WHERE s.stopReason = 'STOP_LOSS_HIT'")
    Long countStopLossHits();

    /**
     * Counts sessions that reached take-profit.
     */
    @Query("SELECT COUNT(s) FROM RouletteSession s WHERE s.stopReason = 'TAKE_PROFIT_REACHED'")
    Long countTakeProfitReached();
}
