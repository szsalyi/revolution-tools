package com.revolution.tools.roulette.entity;

import com.revolution.tools.roulette.enums.BetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity representing a bet placed in a roulette game.
 * Stores bet history for data analysis - NOT tied to sessions.
 *
 * A single bet can contain numbers from MULTIPLE sources:
 * - Custom rules (e.g., previous number 2 → suggests 4, 22)
 * - Hot numbers (e.g., 14, 34 appeared frequently)
 * - Neighbors (e.g., 17 ± 2 neighbors)
 * - Missing numbers
 *
 * Each number tracks its bet source for future pattern analysis.
 */
@Entity
@Table(name = "roulette_bets", indexes = {
    @Index(name = "idx_bet_session_id", columnList = "session_id"),
    @Index(name = "idx_bet_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouletteBet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optional session reference for grouping bets.
     * Can be null for standalone bet analysis.
     */
    @Column(name = "session_id")
    private Long sessionId;

    /**
     * Numbers bet on, stored as CSV: "4,22,14,34"
     */
    @Column(name = "numbers", nullable = false, length = 500)
    private String numbers;

    /**
     * Bet sources/types for each number, stored as CSV parallel to numbers.
     * Example: "CUSTOM_RULE,CUSTOM_RULE,HOT_NUMBER,HOT_NUMBER"
     *
     * This allows data analysis on which patterns produced wins/losses.
     */
    @Column(name = "bet_sources", nullable = false, length = 1000)
    private String betSources;

    /**
     * Stakes for each number, stored as CSV parallel to numbers.
     * Example: "2.00,2.00,0.50,0.50"
     *
     * Enables weighted betting (bingo vs safety numbers).
     * If null, use stakePerNumber for all.
     */
    @Column(name = "stakes", length = 500)
    private String stakes;

    /**
     * Optional: Additional metadata per number as JSON.
     * Example: {"4":"prev_2_rule","22":"prev_2_rule","14":"hot_freq_5","34":"hot_freq_4"}
     */
    @Column(name = "bet_metadata", length = 2000)
    private String betMetadata;

    /**
     * Default stake per number (for uniform bets).
     * If stakes field is set, this is ignored.
     */
    @Column(name = "stake_per_number", precision = 10, scale = 2)
    private BigDecimal stakePerNumber;

    @Column(name = "total_stake", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalStake;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "validated", nullable = false)
    private Boolean validated;

    @Column(name = "validation_result", length = 1000)
    private String validationResult;

    /**
     * The winning number after spin completes.
     */
    @Column(name = "result_spin_number")
    private Integer resultSpinNumber;

    /**
     * Did any of the bet numbers win?
     */
    @Column(name = "is_win")
    private Boolean isWin;

    /**
     * If win, which number(s) won and their source(s).
     * Example: "14:HOT_NUMBER" or "4:CUSTOM_RULE,22:CUSTOM_RULE"
     */
    @Column(name = "winning_sources", length = 200)
    private String winningSources;

    @Column(name = "payout", precision = 10, scale = 2)
    private BigDecimal payout;

    @Column(name = "net_result", precision = 10, scale = 2)
    private BigDecimal netResult; // Payout - total_stake

    /**
     * Legacy field - kept for backward compatibility.
     * Now use betSources for granular tracking.
     */
    @Column(name = "based_on_pattern", length = 50)
    @Deprecated
    private String basedOnPattern;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (validated == null) {
            validated = false;
        }
    }

    /**
     * Gets the list of numbers as integers.
     */
    public List<Integer> getNumberList() {
        if (numbers == null || numbers.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        String[] parts = numbers.split(",");
        List<Integer> result = new ArrayList<>();
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }
        return result;
    }

    /**
     * Gets the list of bet sources as enums, parallel to getNumberList().
     */
    public List<BetType> getBetSourceList() {
        if (betSources == null || betSources.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        String[] parts = betSources.split(",");
        List<BetType> result = new ArrayList<>();
        for (String part : parts) {
            try {
                result.add(BetType.valueOf(part.trim()));
            } catch (IllegalArgumentException e) {
                // Skip invalid bet types
                result.add(BetType.UNKNOWN);
            }
        }
        return result;
    }

    /**
     * Gets the list of stakes as BigDecimal, parallel to getNumberList().
     */
    public List<BigDecimal> getStakeList() {
        if (stakes == null || stakes.isEmpty()) {
            // Use uniform stake for all numbers
            List<BigDecimal> result = new ArrayList<>();
            int count = getNumberList().size();
            for (int i = 0; i < count; i++) {
                result.add(stakePerNumber != null ? stakePerNumber : BigDecimal.ZERO);
            }
            return result;
        }

        String[] parts = stakes.split(",");
        List<BigDecimal> result = new ArrayList<>();
        for (String part : parts) {
            try {
                result.add(new BigDecimal(part.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid stakes
                result.add(BigDecimal.ZERO);
            }
        }
        return result;
    }

    /**
     * Gets a map of number → bet source.
     * Example: {4=CUSTOM_RULE, 22=CUSTOM_RULE, 14=HOT_NUMBER, 34=HOT_NUMBER}
     */
    public Map<Integer, BetType> getNumberSourceMap() {
        List<Integer> nums = getNumberList();
        List<BetType> sources = getBetSourceList();

        Map<Integer, BetType> map = new HashMap<>();
        for (int i = 0; i < Math.min(nums.size(), sources.size()); i++) {
            map.put(nums.get(i), sources.get(i));
        }
        return map;
    }

    /**
     * Gets a map of number → stake amount.
     * Example: {4=2.00, 22=2.00, 14=0.50, 34=0.50}
     */
    public Map<Integer, BigDecimal> getNumberStakeMap() {
        List<Integer> nums = getNumberList();
        List<BigDecimal> stakeAmounts = getStakeList();

        Map<Integer, BigDecimal> map = new HashMap<>();
        for (int i = 0; i < Math.min(nums.size(), stakeAmounts.size()); i++) {
            map.put(nums.get(i), stakeAmounts.get(i));
        }
        return map;
    }

    /**
     * Sets bet data from number-source map (uniform stakes).
     */
    public void setNumberSourceMap(Map<Integer, BetType> numberSourceMap) {
        if (numberSourceMap == null || numberSourceMap.isEmpty()) {
            this.numbers = "";
            this.betSources = "";
            return;
        }

        List<String> numList = new ArrayList<>();
        List<String> sourceList = new ArrayList<>();

        for (Map.Entry<Integer, BetType> entry : numberSourceMap.entrySet()) {
            numList.add(entry.getKey().toString());
            sourceList.add(entry.getValue().name());
        }

        this.numbers = String.join(",", numList);
        this.betSources = String.join(",", sourceList);
    }

    /**
     * Sets bet data from number-source-stake map (weighted stakes).
     *
     * @param betData Map of number → (source, stake)
     */
    public void setWeightedBetData(Map<Integer, BetData> betData) {
        if (betData == null || betData.isEmpty()) {
            this.numbers = "";
            this.betSources = "";
            this.stakes = "";
            return;
        }

        List<String> numList = new ArrayList<>();
        List<String> sourceList = new ArrayList<>();
        List<String> stakeList = new ArrayList<>();

        for (Map.Entry<Integer, BetData> entry : betData.entrySet()) {
            numList.add(entry.getKey().toString());
            sourceList.add(entry.getValue().source.name());
            stakeList.add(entry.getValue().stake.toString());
        }

        this.numbers = String.join(",", numList);
        this.betSources = String.join(",", sourceList);
        this.stakes = String.join(",", stakeList);
    }

    /**
     * Helper class for weighted bet data.
     */
    public static class BetData {
        public BetType source;
        public BigDecimal stake;

        public BetData(BetType source, BigDecimal stake) {
            this.source = source;
            this.stake = stake;
        }
    }

    /**
     * Checks if this bet covers the winning number.
     */
    public boolean coversNumber(int spinNumber) {
        return getNumberList().contains(spinNumber);
    }

    /**
     * Calculates the payout for straight bets (supports weighted stakes).
     * Standard payout is 35:1 for straight bets.
     */
    public void calculateResult(int spinNumber) {
        this.resultSpinNumber = spinNumber;

        if (coversNumber(spinNumber)) {
            this.isWin = true;

            // Find which source(s) and stake won
            Map<Integer, BetType> sourceMap = getNumberSourceMap();
            Map<Integer, BigDecimal> stakeMap = getNumberStakeMap();

            BetType winningSource = sourceMap.get(spinNumber);
            BigDecimal winningStake = stakeMap.getOrDefault(spinNumber, stakePerNumber);

            this.winningSources = spinNumber + ":" + winningSource;

            // For straight bets: 35:1 payout
            this.payout = winningStake.multiply(BigDecimal.valueOf(36)); // 35:1 + stake back
            this.netResult = payout.subtract(totalStake);
        } else {
            this.isWin = false;
            this.winningSources = null;
            this.payout = BigDecimal.ZERO;
            this.netResult = totalStake.negate();
        }
    }

    /**
     * Alias for timestamp to match service expectations.
     */
    public LocalDateTime getPlacedAt() {
        return timestamp;
    }

    /**
     * Alias for timestamp setter to match service expectations.
     */
    public void setPlacedAt(LocalDateTime placedAt) {
        this.timestamp = placedAt;
    }

    /**
     * Alias for netResult to match service expectations.
     */
    public BigDecimal getWinLoss() {
        return netResult;
    }
}
