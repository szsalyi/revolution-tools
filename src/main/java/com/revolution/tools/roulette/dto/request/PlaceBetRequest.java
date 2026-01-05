package com.revolution.tools.roulette.dto.request;

import com.revolution.tools.roulette.dto.BetItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Request to place a bet with weighted stakes.
 *
 * Each bet item specifies a number with its individual stake amount and source.
 *
 * Example usage:
 * <pre>
 * PlaceBetRequest request = new PlaceBetRequest();
 * request.setBets(Arrays.asList(
 *     new BetItem(4, 2.00, BetType.CUSTOM_RULE, "bingo"),    // €2.00
 *     new BetItem(22, 2.00, BetType.CUSTOM_RULE, "bingo"),   // €2.00
 *     new BetItem(14, 0.50, BetType.HOT_NUMBER, "safety"),   // €0.50
 *     new BetItem(34, 0.50, BetType.HOT_NUMBER, "safety")    // €0.50
 * ));
 * // Total stake: €5.00
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBetRequest {

    /**
     * List of bet items with individual stakes.
     * Use this for weighted betting (bingo vs safety numbers).
     */
    @Valid
    @Size(min = 1, max = 20, message = "Must bet on 1-20 numbers")
    private List<BetItem> bets;

    // === Helper Methods ===

    /**
     * Get bet items (never null, returns empty list if bets is null).
     */
    public List<BetItem> getBetItems() {
        return bets != null ? bets : new ArrayList<>();
    }

    /**
     * Calculate total stake across all bet items.
     */
    public BigDecimal getTotalStake() {
        return getBetItems().stream()
                .map(BetItem::getStake)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get all numbers being bet on.
     */
    public List<Integer> getNumbers() {
        return getBetItems().stream()
                .map(BetItem::getNumber)
                .toList();
    }
}

