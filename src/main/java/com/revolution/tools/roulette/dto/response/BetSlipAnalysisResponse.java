package com.revolution.tools.roulette.dto.response;

import com.revolution.tools.roulette.dto.BetItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response from AI-powered bet slip screenshot analysis.
 *
 * Example response:
 * {
 *   "betDetected": true,
 *   "betItems": [
 *     {"number": 4, "stake": 2.00},
 *     {"number": 22, "stake": 2.00},
 *     {"number": 14, "stake": 0.50}
 *   ],
 *   "totalStake": 4.50,
 *   "aiConfidence": 0.95,
 *   "analysisNotes": "Detected 3 numbers with individual stakes",
 *   "rawAiResponse": "I can see a bet slip with numbers 4, 22, 14...",
 *   "betRecorded": true,
 *   "betId": 123
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetSlipAnalysisResponse {

    /**
     * Was a bet successfully detected in the image?
     */
    private Boolean betDetected;

    /**
     * Extracted bet items from the screenshot.
     */
    private List<BetItem> betItems;

    /**
     * Total stake calculated from bet items.
     */
    private BigDecimal totalStake;

    /**
     * AI confidence score (0.0 - 1.0).
     */
    private Double aiConfidence;

    /**
     * Human-readable notes from AI analysis.
     */
    private String analysisNotes;

    /**
     * Raw response from Claude AI (for debugging).
     */
    private String rawAiResponse;

    /**
     * Was the bet automatically recorded?
     */
    private Boolean betRecorded;

    /**
     * ID of the recorded bet (if betRecorded = true).
     */
    private Long betId;

    /**
     * Any warnings from the AI analysis.
     */
    private List<String> warnings;
}
