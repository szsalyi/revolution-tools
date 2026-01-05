package com.revolution.tools.roulette.service;

import com.revolution.tools.roulette.dto.response.SmartBetSuggestion;

/**
 * Service for generating smart bet suggestions with weighted stakes.
 */
public interface SmartBetSuggestionService {

    /**
     * Generate smart bet suggestions based on session history and patterns.
     *
     * @param sessionId Session ID
     * @return Smart bet suggestions with ready-to-use BetItems
     */
    SmartBetSuggestion generateSuggestions(Long sessionId);

    /**
     * Generate suggestions with custom parameters.
     *
     * @param sessionId Session ID
     * @param maxNumbers Maximum numbers to suggest (default: 15)
     * @param bingoStake Stake for high-confidence "bingo" numbers (default: calculated from bankroll)
     * @param safetyStake Stake for backup "safety" numbers (default: 25% of bingo stake)
     * @return Smart bet suggestions
     */
    SmartBetSuggestion generateSuggestions(Long sessionId, Integer maxNumbers,
                                          java.math.BigDecimal bingoStake,
                                          java.math.BigDecimal safetyStake);
}
