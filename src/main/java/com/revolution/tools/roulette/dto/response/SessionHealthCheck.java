package com.revolution.tools.roulette.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Real-time health check for current session.
 * Warns about overbetting, rule violations, and emotional betting.
 *
 * Example response:
 * {
 *   "healthy": false,
 *   "status": "WARNING",
 *   "currentBankroll": 450.00,
 *   "profitLoss": -50.00,
 *   "profitPercent": -10.0,
 *   "stopLossDistance": 40.0,
 *   "takeProfitDistance": 60.0,
 *   "recommendedMaxStake": 13.50,
 *   "currentAverageStake": 18.00,
 *   "tiltDetected": true,
 *   "violations": 3,
 *   "warnings": [
 *     "TILT DETECTED: You're betting emotionally",
 *     "OVERBETTING: Your average stake (18.00) exceeds recommended (13.50)",
 *     "STOP-LOSS CLOSE: Only 40% away from stop-loss"
 *   ],
 *   "recommendations": [
 *     "Take a break - tilt detected",
 *     "Reduce stake to max 13.50",
 *     "Consider stopping - close to stop-loss"
 *   ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionHealthCheck {

    /**
     * Overall health status: true if all checks pass.
     */
    private Boolean healthy;

    /**
     * Status level: OK, WARNING, CRITICAL.
     */
    private String status;

    /**
     * Current bankroll amount.
     */
    private BigDecimal currentBankroll;

    /**
     * Current profit/loss amount.
     */
    private BigDecimal profitLoss;

    /**
     * Current profit/loss percentage.
     */
    private Double profitPercent;

    /**
     * How close to stop-loss (percentage points).
     * Example: 40.0 = 40% away from hitting stop-loss
     */
    private Double stopLossDistance;

    /**
     * How close to take-profit (percentage points).
     */
    private Double takeProfitDistance;

    /**
     * Recommended maximum stake per bet based on discipline rules.
     */
    private BigDecimal recommendedMaxStake;

    /**
     * Your current average stake across recent bets.
     */
    private BigDecimal currentAverageStake;

    /**
     * Is tilt detected? (emotional/revenge betting)
     */
    private Boolean tiltDetected;

    /**
     * Number of rule violations in this session.
     */
    private Integer violations;

    /**
     * Total spins so far.
     */
    private Integer totalSpins;

    /**
     * Max spins allowed before session auto-stops.
     */
    private Integer maxSpins;

    /**
     * Warning messages about current betting behavior.
     */
    private List<String> warnings;

    /**
     * Actionable recommendations.
     */
    private List<String> recommendations;

    /**
     * Session stop reason (if session is stopped).
     */
    private String stopReason;
}
