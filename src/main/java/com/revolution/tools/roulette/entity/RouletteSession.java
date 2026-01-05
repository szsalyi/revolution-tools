package com.revolution.tools.roulette.entity;

import com.revolution.tools.roulette.enums.SessionStatus;
import com.revolution.tools.roulette.enums.StopReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a roulette playing session.
 * Tracks bankroll, profit/loss, and discipline metrics.
 */
@Entity
@Table(name = "roulette_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouletteSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false, length = 36)
    private String sessionId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "initial_bankroll", nullable = false, precision = 10, scale = 2)
    private BigDecimal initialBankroll;

    @Column(name = "current_bankroll", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentBankroll;

    @Column(name = "peak_profit", precision = 10, scale = 2)
    private BigDecimal peakProfit;

    @Column(name = "current_profit", precision = 10, scale = 2)
    private BigDecimal currentProfit;

    @Column(name = "total_spins")
    private Integer totalSpins;

    @Column(name = "total_bets")
    private Integer totalBets;

    @Column(name = "total_wins")
    private Integer totalWins;

    @Column(name = "total_losses")
    private Integer totalLosses;

    @Column(name = "stop_reason", length = 50)
    @Enumerated(EnumType.STRING)
    private StopReason stopReason;

    @Column(name = "stop_loss_percent")
    private Integer stopLossPercent;

    @Column(name = "take_profit_levels", length = 100)
    private String takeProfitLevels; // Stored as comma-separated: "70,130"

    @Column(name = "flat_bet_min_percent")
    private Integer flatBetMinPercent;

    @Column(name = "flat_bet_max_percent")
    private Integer flatBetMaxPercent;

    @Column(name = "max_spins")
    private Integer maxSpins;

    @Column(name = "max_duration_minutes")
    private Integer maxDurationMinutes;

    @Column(name = "rule_violations")
    private Integer ruleViolations;

    @Column(name = "tilt_events")
    private Integer tiltEvents;

    @Column(name = "profit_protected", precision = 10, scale = 2)
    private BigDecimal profitProtected;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (totalSpins == null) totalSpins = 0;
        if (totalBets == null) totalBets = 0;
        if (totalWins == null) totalWins = 0;
        if (totalLosses == null) totalLosses = 0;
        if (ruleViolations == null) ruleViolations = 0;
        if (tiltEvents == null) tiltEvents = 0;
        if (peakProfit == null) peakProfit = BigDecimal.ZERO;
        if (currentProfit == null) currentProfit = BigDecimal.ZERO;
        if (profitProtected == null) profitProtected = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates current profit/loss percentage.
     */
    public BigDecimal getProfitPercent() {
        if (initialBankroll.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentProfit.divide(initialBankroll, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Checks if stop-loss has been hit.
     */
    public boolean isStopLossHit() {
        if (stopLossPercent == null) return false;
        return getProfitPercent().compareTo(BigDecimal.valueOf(stopLossPercent)) <= 0;
    }

    /**
     * Checks if any take-profit level has been reached.
     */
    public boolean isTakeProfitReached() {
        if (takeProfitLevels == null || takeProfitLevels.isEmpty()) return false;

        BigDecimal profitPercent = getProfitPercent();
        String[] levels = takeProfitLevels.split(",");

        for (String level : levels) {
            try {
                int targetPercent = Integer.parseInt(level.trim());
                if (profitPercent.compareTo(BigDecimal.valueOf(targetPercent)) >= 0) {
                    return true;
                }
            } catch (NumberFormatException e) {
                // Skip invalid levels
            }
        }
        return false;
    }

    /**
     * Checks if session has exceeded max spins.
     */
    public boolean isMaxSpinsReached() {
        if (maxSpins == null) return false;
        return totalSpins >= maxSpins;
    }

    /**
     * Checks if session has exceeded max duration.
     */
    public boolean isMaxDurationReached() {
        if (maxDurationMinutes == null || startTime == null) return false;
        LocalDateTime maxEndTime = startTime.plusMinutes(maxDurationMinutes);
        return LocalDateTime.now().isAfter(maxEndTime);
    }

    /**
     * Updates bankroll after a bet result.
     */
    public void updateBankroll(BigDecimal winLoss) {
        currentBankroll = currentBankroll.add(winLoss);
        currentProfit = currentBankroll.subtract(initialBankroll);

        // Update peak profit
        if (currentProfit.compareTo(peakProfit) > 0) {
            peakProfit = currentProfit;
        }
    }

    /**
     * Increments rule violation counter.
     */
    public void recordRuleViolation() {
        ruleViolations = (ruleViolations != null ? ruleViolations : 0) + 1;
    }

    /**
     * Increments tilt event counter.
     */
    public void recordTiltEvent() {
        tiltEvents = (tiltEvents != null ? tiltEvents : 0) + 1;
    }

    /**
     * Converts take profit levels string to list of integers.
     */
    public java.util.List<Integer> getTakeProfitLevelsAsList() {
        if (takeProfitLevels == null || takeProfitLevels.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        String[] levels = takeProfitLevels.split(",");
        java.util.List<Integer> result = new java.util.ArrayList<>();
        for (String level : levels) {
            try {
                result.add(Integer.parseInt(level.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid levels
            }
        }
        return result;
    }

    /**
     * Sets take profit levels from a list.
     */
    public void setTakeProfitLevelsFromList(java.util.List<Integer> levels) {
        if (levels == null || levels.isEmpty()) {
            this.takeProfitLevels = null;
        } else {
            this.takeProfitLevels = levels.stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(","));
        }
    }

    /**
     * Alias for startTime to match service expectations.
     */
    public LocalDateTime getStartedAt() {
        return startTime;
    }

    /**
     * Alias for endTime to match service expectations.
     */
    public LocalDateTime getEndedAt() {
        return endTime;
    }

    /**
     * Alias for setEndTime to match service expectations.
     */
    public void setEndedAt(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
