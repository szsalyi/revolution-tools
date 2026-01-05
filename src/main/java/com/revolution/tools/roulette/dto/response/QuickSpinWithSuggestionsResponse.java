package com.revolution.tools.roulette.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enhanced response for quick spin recording.
 * Includes spin confirmation + next bet suggestions + session health.
 *
 * Production-ready: One API call gets everything you need!
 *
 * Example response:
 * {
 *   "spinRecorded": {
 *     "spinNumber": 17,
 *     "sequenceNumber": 26,
 *     "color": "BLACK"
 *   },
 *   "nextSuggestions": {
 *     "betItems": [...],
 *     "confidence": "HIGH",
 *     "reasoning": "Section 17-11 now at 40% hit rate"
 *   },
 *   "sessionHealth": {
 *     "healthy": true,
 *     "currentBankroll": 485.00,
 *     "warnings": []
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickSpinWithSuggestionsResponse {

    /**
     * Confirmation of recorded spin.
     */
    private SpinResponse spinRecorded;

    /**
     * Next bet suggestions based on updated history.
     */
    private SmartBetSuggestion nextSuggestions;

    /**
     * Current session health status.
     */
    private SessionHealthCheck sessionHealth;

    /**
     * Updated session status (bankroll, profit, etc.)
     */
    private SessionResponse sessionStatus;
}
