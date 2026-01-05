package com.revolution.tools.roulette.dto.response;

import com.revolution.tools.roulette.enums.RuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a custom rule that matched and suggests numbers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedRule {

    /**
     * Rule ID.
     */
    private Long ruleId;

    /**
     * Rule name.
     */
    private String name;

    /**
     * Rule description.
     */
    private String description;

    /**
     * Rule type.
     */
    private RuleType ruleType;

    /**
     * Numbers that triggered this rule.
     */
    private List<Integer> triggerNumbers;

    /**
     * Numbers suggested by this rule.
     */
    private List<Integer> suggestedNumbers;

    /**
     * Confidence level (1-100).
     */
    private Integer confidence;

    /**
     * Hit rate (success percentage).
     */
    private Double hitRate;

    /**
     * Why this rule was triggered (explanation for user).
     */
    private String reason;
}
