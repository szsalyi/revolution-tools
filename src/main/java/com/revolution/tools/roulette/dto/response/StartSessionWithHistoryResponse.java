package com.revolution.tools.roulette.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response when starting a session with historical data.
 * Includes session info + initial bet suggestions.
 *
 * Example response:
 * {
 *   "session": {
 *     "sessionId": "abc-123-xyz",
 *     "initialBankroll": 500.00,
 *     "status": "ACTIVE"
 *   },
 *   "spinsImported": 25,
 *   "initialSuggestions": {
 *     "betItems": [...],
 *     "confidence": "HIGH",
 *     "reasoning": "Section 0-25 showing 35% hit rate"
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartSessionWithHistoryResponse {

    /**
     * Created session details.
     */
    private SessionResponse session;

    /**
     * Number of historical spins imported.
     */
    private Integer spinsImported;

    /**
     * Initial bet suggestions based on historical data.
     */
    private SmartBetSuggestion initialSuggestions;

    /**
     * Detected patterns from historical data.
     */
    private String detectedPatterns;
}
