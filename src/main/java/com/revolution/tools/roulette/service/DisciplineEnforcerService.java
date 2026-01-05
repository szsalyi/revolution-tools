package com.revolution.tools.roulette.service;

import com.revolution.tools.roulette.entity.RouletteSession;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for enforcing gambling discipline rules.
 * Checks stop-loss, take-profit, tilt detection, and stake validation.
 */
public interface DisciplineEnforcerService {

    /**
     * Check if session should be stopped based on discipline rules.
     *
     * @param session Session to check
     * @return true if session should be stopped
     */
    boolean shouldStopSession(RouletteSession session);

    /**
     * Validate stake amount against discipline rules.
     *
     * @param session Session
     * @param stakePerNumber Stake per number
     * @param numberCount Number of numbers being bet
     * @return Validation messages (empty if valid)
     */
    List<String> validateStake(RouletteSession session, BigDecimal stakePerNumber, int numberCount);

    /**
     * Detect tilt behavior.
     * Tilt: stake increase >50% in last 5 bets OR 3+ rule violations in 10 bets.
     *
     * @param sessionId Session identifier
     * @return true if tilt detected
     */
    boolean detectTilt(Long sessionId);

    /**
     * Calculate recommended stake based on flat betting rule.
     *
     * @param session Session
     * @return Recommended stake per number
     */
    BigDecimal calculateRecommendedStake(RouletteSession session);

    /**
     * Check if stop-loss has been hit.
     *
     * @param session Session to check
     * @return true if stop-loss hit
     */
    boolean isStopLossHit(RouletteSession session);

    /**
     * Check if take-profit level has been reached.
     *
     * @param session Session to check
     * @return true if take-profit reached
     */
    boolean isTakeProfitReached(RouletteSession session);

    /**
     * Check if max spins limit has been reached.
     *
     * @param session Session to check
     * @return true if max spins reached
     */
    boolean isMaxSpinsReached(RouletteSession session);
}
