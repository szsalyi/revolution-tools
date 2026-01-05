package com.revolution.tools.roulette.dto.response;

import com.revolution.tools.roulette.dto.BetItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Smart bet suggestions with ready-to-use weighted BetItems.
 * Production-ready: Returns exactly what you need to place a bet.
 *
 * Example response:
 * {
 *   "betItems": [
 *     {"number": 4, "stake": 2.00, "source": "CUSTOM_RULE", "tier": "bingo"},
 *     {"number": 22, "stake": 2.00, "source": "CUSTOM_RULE", "tier": "bingo"},
 *     {"number": 14, "stake": 0.50, "source": "HOT_NUMBER", "tier": "safety"}
 *   ],
 *   "totalStake": 4.50,
 *   "recommendedStake": 5.00,
 *   "bingoNumbers": [4, 22],
 *   "safetyNumbers": [14, 34, 17],
 *   "reasoning": "Custom rule suggests 4,22 from previous spin 2. Hot numbers: 14,34,17",
 *   "confidence": "HIGH",
 *   "warnings": []
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartBetSuggestion {

    /**
     * Ready-to-use bet items with weighted stakes.
     * You can directly use this in PlaceBetRequest.
     */
    private List<BetItem> betItems;

    /**
     * Total stake for all bet items.
     */
    private BigDecimal totalStake;

    /**
     * Recommended total stake based on bankroll and discipline rules.
     */
    private BigDecimal recommendedStake;

    /**
     * High-confidence "bingo" numbers (higher stakes).
     */
    private List<Integer> bingoNumbers;

    /**
     * Backup "safety" numbers (lower stakes).
     */
    private List<Integer> safetyNumbers;

    /**
     * Explanation of why these numbers were chosen.
     */
    private String reasoning;

    /**
     * Confidence level: HIGH, MEDIUM, LOW.
     */
    private String confidence;

    /**
     * Any warnings or concerns about this bet.
     */
    private List<String> warnings;

    /**
     * Detected wheel sections (if section clustering detected).
     * Example: "0-25:35.0% + 8-1:30.0%"
     */
    private String hotSections;

    /**
     * Custom rules that were matched and contributed to suggestions.
     */
    private List<MatchedRule> matchedRules;
}
