package com.revolution.tools.roulette.dto.response;

import com.revolution.tools.roulette.enums.SessionStatus;
import com.revolution.tools.roulette.enums.StopReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response containing session details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private Long id;
    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SessionStatus status;

    // Bankroll info
    private BigDecimal initialBankroll;
    private BigDecimal currentBankroll;
    private BigDecimal peakProfit;
    private BigDecimal currentProfit;
    private BigDecimal profitPercent;

    // Session stats
    private Integer totalSpins;
    private Integer totalBets;
    private Integer totalWins;
    private Integer totalLosses;
    private Double winRate;

    // Discipline metrics
    private Integer ruleViolations;
    private Integer tiltEvents;
    private StopReason stopReason;

    // Configuration
    private Integer stopLossPercent;
    private List<Integer> takeProfitLevels;
    private Integer flatBetPercent;
    private Integer maxSpins;
    private Integer maxDurationMinutes;

    // Alerts
    private Integer activeAlertCount;
    private Boolean hasActiveCriticalAlerts;

    // Discipline checks
    private Boolean stopLossHit;
    private Boolean takeProfitReached;
    private Boolean maxSpinsReached;
    private Boolean maxDurationReached;
    private Boolean canContinuePlaying;

    private String message;
    private String notes;
}
