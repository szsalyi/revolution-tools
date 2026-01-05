package com.revolution.tools.roulette.dto;

import com.revolution.tools.roulette.enums.BetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a single number bet with its stake amount and source.
 *
 * Enables weighted betting strategies:
 * - Bingo numbers (high confidence) → higher stake
 * - Safety numbers (backup) → lower stake
 *
 * Example:
 * <pre>
 * // High confidence from custom rule
 * BetItem bingo1 = new BetItem(4, 2.00, CUSTOM_RULE, "bingo");
 * BetItem bingo2 = new BetItem(22, 2.00, CUSTOM_RULE, "bingo");
 *
 * // Backup from hot numbers
 * BetItem safety1 = new BetItem(14, 0.50, HOT_NUMBER, "safety");
 * BetItem safety2 = new BetItem(34, 0.50, HOT_NUMBER, "safety");
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetItem {

    /**
     * The roulette number (0-36).
     */
    @NotNull(message = "Number is required")
    @Min(value = 0, message = "Number must be between 0 and 36")
    @Max(value = 36, message = "Number must be between 0 and 36")
    private Integer number;

    /**
     * Stake amount for this specific number.
     */
    @NotNull(message = "Stake is required")
    @DecimalMin(value = "0.10", message = "Minimum stake is 0.10")
    private BigDecimal stake;

    /**
     * Why this number was chosen (pattern source).
     * Optional - defaults to UNKNOWN if not specified.
     */
    private BetType source;

    /**
     * Optional tier/category for this number.
     * Examples: "bingo", "safety", "primary", "backup"
     *
     * Useful for analyzing which tiers perform better.
     */
    private String tier;

    /**
     * Optional metadata/notes.
     * Example: "prev_2_rule", "hot_freq_5", "neighbor_of_17"
     */
    private String metadata;

    /**
     * Convenience constructor for number and stake only.
     */
    public BetItem(Integer number, BigDecimal stake) {
        this.number = number;
        this.stake = stake;
        this.source = BetType.UNKNOWN;
    }

    /**
     * Convenience constructor for number, stake, and source.
     */
    public BetItem(Integer number, BigDecimal stake, BetType source) {
        this.number = number;
        this.stake = stake;
        this.source = source;
    }

    /**
     * Convenience constructor for number, stake, source, and tier.
     */
    public BetItem(Integer number, BigDecimal stake, BetType source, String tier) {
        this.number = number;
        this.stake = stake;
        this.source = source;
        this.tier = tier;
    }

    /**
     * Convenience constructor with double stake (converts to BigDecimal).
     */
    public BetItem(Integer number, double stake, BetType source) {
        this.number = number;
        this.stake = BigDecimal.valueOf(stake);
        this.source = source;
    }

    /**
     * Convenience constructor with double stake and tier.
     */
    public BetItem(Integer number, double stake, BetType source, String tier) {
        this.number = number;
        this.stake = BigDecimal.valueOf(stake);
        this.source = source;
        this.tier = tier;
    }

    /**
     * Get source or default to UNKNOWN.
     */
    public BetType getSource() {
        return source != null ? source : BetType.UNKNOWN;
    }
}
