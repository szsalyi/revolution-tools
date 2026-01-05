package com.revolution.tools.roulette.dto;

import com.revolution.tools.roulette.entity.RouletteBet;
import com.revolution.tools.roulette.enums.BetType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for building multi-source roulette bets.
 *
 * Makes it easy to combine numbers from different patterns:
 * - Custom rules (e.g., prev 2 → 4, 22)
 * - Hot numbers
 * - Neighbors
 * - Missing numbers
 *
 * Example usage:
 * <pre>
 * BetBuilder builder = new BetBuilder(sessionId);
 * builder.addNumber(4, BetType.CUSTOM_RULE);   // From custom rule: 2→4
 * builder.addNumber(22, BetType.CUSTOM_RULE);  // From custom rule: 2→22
 * builder.addNumber(14, BetType.HOT_NUMBER);   // Hot number
 * builder.addNumber(34, BetType.HOT_NUMBER);   // Hot number
 * builder.addNeighbors(14, 2);                 // Add neighbors of 14
 *
 * RouletteBet bet = builder.build(new BigDecimal("1.00"));
 * // Result: numbers=[4,22,14,34,15,19,...], sources=[CUSTOM_RULE,CUSTOM_RULE,HOT_NUMBER,HOT_NUMBER,NEIGHBOR,NEIGHBOR,...]
 * </pre>
 */
@Data
public class BetBuilder {

    private Long sessionId;
    private Map<Integer, BetType> numberSourceMap;
    private Map<Integer, BigDecimal> numberStakeMap;
    private Map<Integer, String> numberMetadata;
    private Map<Integer, String> numberTierMap;

    // European roulette wheel order
    private static final int[] WHEEL_ORDER = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
            5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };

    public BetBuilder() {
        this.numberSourceMap = new LinkedHashMap<>();
        this.numberStakeMap = new LinkedHashMap<>();
        this.numberMetadata = new HashMap<>();
        this.numberTierMap = new HashMap<>();
    }

    public BetBuilder(Long sessionId) {
        this();
        this.sessionId = sessionId;
    }

    /**
     * Add a single number with its source (uniform stake).
     */
    public BetBuilder addNumber(Integer number, BetType source) {
        return addNumber(number, source, null, null, null);
    }

    /**
     * Add a single number with source and metadata.
     */
    public BetBuilder addNumber(Integer number, BetType source, String metadata) {
        return addNumber(number, source, null, metadata, null);
    }

    /**
     * Add a single number with source and custom stake (weighted betting).
     */
    public BetBuilder addNumber(Integer number, BetType source, BigDecimal stake) {
        return addNumber(number, source, stake, null, null);
    }

    /**
     * Add a single number with source, stake, and tier (full control).
     */
    public BetBuilder addNumber(Integer number, BetType source, BigDecimal stake, String tier) {
        return addNumber(number, source, stake, null, tier);
    }

    /**
     * Add a single number with all parameters.
     */
    public BetBuilder addNumber(Integer number, BetType source, BigDecimal stake, String metadata, String tier) {
        if (number < 0 || number > 36) {
            throw new IllegalArgumentException("Number must be between 0 and 36");
        }
        numberSourceMap.put(number, source);

        if (stake != null) {
            numberStakeMap.put(number, stake);
        }
        if (metadata != null) {
            numberMetadata.put(number, metadata);
        }
        if (tier != null) {
            numberTierMap.put(number, tier);
        }
        return this;
    }

    /**
     * Add multiple numbers with the same source (uniform stake).
     */
    public BetBuilder addNumbers(List<Integer> numbers, BetType source) {
        for (Integer number : numbers) {
            addNumber(number, source);
        }
        return this;
    }

    /**
     * Add multiple numbers with the same source and stake (weighted).
     */
    public BetBuilder addNumbers(List<Integer> numbers, BetType source, BigDecimal stake) {
        for (Integer number : numbers) {
            addNumber(number, source, stake);
        }
        return this;
    }

    /**
     * Add multiple numbers with the same source, stake, and tier.
     */
    public BetBuilder addNumbers(List<Integer> numbers, BetType source, BigDecimal stake, String tier) {
        for (Integer number : numbers) {
            addNumber(number, source, stake, tier);
        }
        return this;
    }

    /**
     * Add neighbors of a number on the wheel.
     *
     * @param centerNumber The center number
     * @param distance Number of neighbors on each side (e.g., 2 = ±2 neighbors)
     */
    public BetBuilder addNeighbors(Integer centerNumber, int distance) {
        List<Integer> neighbors = getNeighbors(centerNumber, distance);
        for (Integer neighbor : neighbors) {
            if (!numberSourceMap.containsKey(neighbor)) { // Don't override existing sources
                addNumber(neighbor, BetType.NEIGHBOR, "neighbor_of_" + centerNumber);
            }
        }
        return this;
    }

    /**
     * Add custom rule numbers (uniform stake).
     * Example: Previous spin 2 → suggests 4, 22
     */
    public BetBuilder addCustomRuleNumbers(Integer previousNumber, List<Integer> suggestedNumbers) {
        for (Integer number : suggestedNumbers) {
            addNumber(number, BetType.CUSTOM_RULE, "prev_" + previousNumber + "_rule");
        }
        return this;
    }

    /**
     * Add custom rule numbers with specific stake (weighted - "bingo" numbers).
     */
    public BetBuilder addCustomRuleNumbers(Integer previousNumber, List<Integer> suggestedNumbers, BigDecimal stake) {
        for (Integer number : suggestedNumbers) {
            addNumber(number, BetType.CUSTOM_RULE, stake, "prev_" + previousNumber + "_rule", "bingo");
        }
        return this;
    }

    /**
     * Add hot numbers detected from pattern analysis (uniform stake).
     */
    public BetBuilder addHotNumbers(List<Integer> hotNumbers) {
        return addNumbers(hotNumbers, BetType.HOT_NUMBER);
    }

    /**
     * Add hot numbers with specific stake (weighted).
     */
    public BetBuilder addHotNumbers(List<Integer> hotNumbers, BigDecimal stake) {
        return addNumbers(hotNumbers, BetType.HOT_NUMBER, stake);
    }

    /**
     * Add missing/cold numbers (uniform stake).
     */
    public BetBuilder addMissingNumbers(List<Integer> missingNumbers) {
        return addNumbers(missingNumbers, BetType.MISSING_NUMBER);
    }

    /**
     * Add missing/cold numbers with specific stake (weighted - "safety" numbers).
     */
    public BetBuilder addMissingNumbers(List<Integer> missingNumbers, BigDecimal stake) {
        return addNumbers(missingNumbers, BetType.MISSING_NUMBER, stake, "safety");
    }

    /**
     * Build the RouletteBet entity with uniform stakes.
     *
     * @param stakePerNumber Stake amount per number (uniform)
     * @return RouletteBet with multi-source data
     */
    public RouletteBet build(BigDecimal stakePerNumber) {
        if (numberSourceMap.isEmpty()) {
            throw new IllegalStateException("Cannot build bet with no numbers");
        }

        RouletteBet bet = new RouletteBet();
        bet.setSessionId(sessionId);
        bet.setNumberSourceMap(numberSourceMap);
        bet.setStakePerNumber(stakePerNumber);
        bet.setTotalStake(stakePerNumber.multiply(BigDecimal.valueOf(numberSourceMap.size())));
        bet.setValidated(false);

        // Build metadata JSON if any
        if (!numberMetadata.isEmpty()) {
            bet.setBetMetadata(buildMetadataJson());
        }

        return bet;
    }

    /**
     * Build the RouletteBet entity with weighted stakes.
     * Uses the custom stakes set via addNumber(..., stake).
     *
     * @return RouletteBet with weighted multi-source data
     */
    public RouletteBet buildWeighted() {
        if (numberSourceMap.isEmpty()) {
            throw new IllegalStateException("Cannot build bet with no numbers");
        }

        // Build weighted bet data map
        Map<Integer, RouletteBet.BetData> betDataMap = new LinkedHashMap<>();
        for (Integer number : numberSourceMap.keySet()) {
            BetType source = numberSourceMap.get(number);
            BigDecimal stake = numberStakeMap.getOrDefault(number, BigDecimal.ONE); // Default to 1.00 if not set

            betDataMap.put(number, new RouletteBet.BetData(source, stake));
        }

        RouletteBet bet = new RouletteBet();
        bet.setSessionId(sessionId);
        bet.setWeightedBetData(betDataMap);

        // Calculate total stake
        BigDecimal totalStake = numberStakeMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bet.setTotalStake(totalStake);
        bet.setValidated(false);

        // Build metadata JSON if any
        if (!numberMetadata.isEmpty()) {
            bet.setBetMetadata(buildMetadataJson());
        }

        return bet;
    }

    /**
     * Check if builder has weighted stakes.
     */
    public boolean hasWeightedStakes() {
        return !numberStakeMap.isEmpty();
    }

    /**
     * Get the current number count.
     */
    public int getNumberCount() {
        return numberSourceMap.size();
    }

    /**
     * Clear all numbers.
     */
    public BetBuilder clear() {
        numberSourceMap.clear();
        numberStakeMap.clear();
        numberMetadata.clear();
        numberTierMap.clear();
        return this;
    }

    /**
     * Get summary of bet sources.
     * Example: {CUSTOM_RULE=2, HOT_NUMBER=2, NEIGHBOR=3}
     */
    public Map<BetType, Long> getSourceSummary() {
        return numberSourceMap.values().stream()
                .collect(Collectors.groupingBy(type -> type, Collectors.counting()));
    }

    // Helper methods

    private List<Integer> getNeighbors(Integer number, int distance) {
        List<Integer> neighbors = new ArrayList<>();

        int position = -1;
        for (int i = 0; i < WHEEL_ORDER.length; i++) {
            if (WHEEL_ORDER[i] == number) {
                position = i;
                break;
            }
        }

        if (position == -1) {
            return neighbors;
        }

        for (int i = -distance; i <= distance; i++) {
            if (i == 0) continue; // Skip center number itself
            int neighborPosition = (position + i + WHEEL_ORDER.length) % WHEEL_ORDER.length;
            neighbors.add(WHEEL_ORDER[neighborPosition]);
        }

        return neighbors;
    }

    private String buildMetadataJson() {
        // Simple JSON format: {"4":"prev_2_rule","22":"prev_2_rule",...}
        return "{" + numberMetadata.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(Collectors.joining(",")) + "}";
    }

    @Override
    public String toString() {
        return "BetBuilder{" +
                "numbers=" + numberSourceMap.keySet() +
                ", sources=" + getSourceSummary() +
                ", count=" + getNumberCount() +
                '}';
    }
}
