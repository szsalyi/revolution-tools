package com.revolution.tools.roulette.service;

import com.revolution.tools.roulette.dto.response.MatchedRule;
import com.revolution.tools.roulette.entity.CustomRule;

import java.util.List;

/**
 * Service for managing and evaluating custom betting rules.
 */
public interface CustomRuleService {

    /**
     * Evaluate all enabled rules against recent spins.
     *
     * @param sessionId Session ID
     * @return List of matched rules with suggested numbers
     */
    List<MatchedRule> evaluateRules(Long sessionId);

    /**
     * Evaluate rules with specific lookback window.
     *
     * @param sessionId Session ID
     * @param lookbackSpins Number of recent spins to analyze
     * @return List of matched rules
     */
    List<MatchedRule> evaluateRules(Long sessionId, Integer lookbackSpins);

    /**
     * Create or update a custom rule.
     *
     * @param rule Rule to save
     * @return Saved rule
     */
    CustomRule saveRule(CustomRule rule);

    /**
     * Get all enabled rules.
     *
     * @return List of enabled rules
     */
    List<CustomRule> getAllEnabledRules();

    /**
     * Get rule by ID.
     *
     * @param id Rule ID
     * @return Rule
     */
    CustomRule getRuleById(Long id);

    /**
     * Enable or disable a rule.
     *
     * @param id Rule ID
     * @param enabled Whether to enable
     * @return Updated rule
     */
    CustomRule toggleRule(Long id, Boolean enabled);

    /**
     * Delete a rule.
     *
     * @param id Rule ID
     */
    void deleteRule(Long id);

    /**
     * Initialize default rules (your predefined patterns).
     * Only creates if they don't exist.
     */
    void initializeDefaultRules();
}
