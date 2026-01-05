package com.revolution.tools.roulette.repository;

import com.revolution.tools.roulette.entity.RouletteBet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for RouletteBet entities.
 */
@Repository
public interface RouletteBetRepository extends JpaRepository<RouletteBet, Long> {

    /**
     * Finds all bets for a session.
     */
    List<RouletteBet> findBySessionIdOrderByTimestampDesc(Long sessionId);

    /**
     * Finds the most recent N bets for a session.
     */
    @Query(value = "SELECT * FROM roulette_bets WHERE session_id = :sessionId ORDER BY timestamp DESC LIMIT :limit", nativeQuery = true)
    List<RouletteBet> findRecentBets(@Param("sessionId") Long sessionId, @Param("limit") int limit);

    /**
     * Finds the last N bets for a session (alias for findRecentBets).
     */
    @Query(value = "SELECT * FROM roulette_bets WHERE session_id = :sessionId ORDER BY timestamp DESC LIMIT :limit", nativeQuery = true)
    List<RouletteBet> findLastNBets(@Param("sessionId") Long sessionId, @Param("limit") int limit);

    /**
     * Finds all pending bets (no result yet) for a session.
     */
    List<RouletteBet> findBySessionIdAndResultSpinNumberIsNull(Long sessionId);

    /**
     * Finds all winning bets for a session.
     */
    List<RouletteBet> findBySessionIdAndIsWin(Long sessionId, Boolean isWin);

    /**
     * Counts total bets for a session.
     */
    Long countBySessionId(Long sessionId);

    /**
     * Counts winning bets for a session.
     */
    Long countBySessionIdAndIsWin(Long sessionId, Boolean isWin);

    /**
     * Calculates total amount staked in a session.
     */
    @Query("SELECT COALESCE(SUM(b.totalStake), 0) FROM RouletteBet b WHERE b.sessionId = :sessionId")
    BigDecimal calculateTotalStaked(@Param("sessionId") Long sessionId);

    /**
     * Calculates total winnings in a session.
     */
    @Query("SELECT COALESCE(SUM(b.payout), 0) FROM RouletteBet b WHERE b.sessionId = :sessionId AND b.isWin = true")
    BigDecimal calculateTotalWinnings(@Param("sessionId") Long sessionId);

    /**
     * Finds bets that were not validated (rule violations).
     */
    List<RouletteBet> findBySessionIdAndValidated(Long sessionId, Boolean validated);

    /**
     * Finds the top 10 most recent bets for a session.
     */
    List<RouletteBet> findTop10BySessionIdOrderByTimestampDesc(Long sessionId);
}
