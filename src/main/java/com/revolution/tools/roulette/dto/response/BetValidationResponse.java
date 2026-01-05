package com.revolution.tools.roulette.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response from bet validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetValidationResponse {

    private Long betId;
    private Boolean valid;
    private String message;
    private List<String> warnings;
    private List<String> violations;

    // Stake validation
    private BigDecimal totalStake;
    private BigDecimal recommendedStake;
    private Boolean stakeWithinLimits;

    // Pattern validation
    private Boolean matchesPattern;
    private String matchedPattern;

    // Discipline checks
    private Boolean wouldCauseTilt;
    private Boolean wouldViolateRules;

    // Suggestions
    private List<Integer> suggestedNumbers;
    private String suggestionReason;
}
