package com.revolution.tools.roulette.service.impl;

import com.revolution.tools.roulette.dto.BetItem;
import com.revolution.tools.roulette.dto.response.MatchedRule;
import com.revolution.tools.roulette.dto.response.PatternSuggestionResponse;
import com.revolution.tools.roulette.dto.response.SmartBetSuggestion;
import com.revolution.tools.roulette.entity.RouletteSession;
import com.revolution.tools.roulette.enums.BetType;
import com.revolution.tools.roulette.repository.RouletteSessionRepository;
import com.revolution.tools.roulette.service.CustomRuleService;
import com.revolution.tools.roulette.service.DisciplineEnforcerService;
import com.revolution.tools.roulette.service.PatternAnalyzerService;
import com.revolution.tools.roulette.service.SmartBetSuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of SmartBetSuggestionService.
 * Builds intelligent bet suggestions with weighted stakes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartBetSuggestionServiceImpl implements SmartBetSuggestionService {

    private final RouletteSessionRepository sessionRepository;
    private final PatternAnalyzerService patternAnalyzer;
    private final DisciplineEnforcerService disciplineEnforcer;
    private final CustomRuleService customRuleService;

    @Override
    @Transactional(readOnly = true)
    public SmartBetSuggestion generateSuggestions(Long sessionId) {
        RouletteSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // Calculate default stakes from bankroll
        BigDecimal recommendedStake = disciplineEnforcer.calculateRecommendedStake(session);
        BigDecimal bingoStake = recommendedStake.divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP); // 20% per bingo number
        BigDecimal safetyStake = bingoStake.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP); // 25% of bingo

        return generateSuggestions(sessionId, 15, bingoStake, safetyStake);
    }

    @Override
    @Transactional(readOnly = true)
    public SmartBetSuggestion generateSuggestions(Long sessionId, Integer maxNumbers,
                                                  BigDecimal bingoStake, BigDecimal safetyStake) {
        log.info("Generating smart suggestions for session {}: maxNumbers={}, bingoStake={}, safetyStake={}",
                sessionId, maxNumbers, bingoStake, safetyStake);

        RouletteSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // Get pattern analysis
        PatternSuggestionResponse patterns = patternAnalyzer.analyzePatterns(sessionId);

        // === EVALUATE CUSTOM RULES (HIGHEST PRIORITY) ===
        List<MatchedRule> matchedRules = customRuleService.evaluateRules(sessionId);
        log.info("Matched {} custom rules", matchedRules.size());

        // Build weighted bet items
        List<BetItem> betItems = new ArrayList<>();
        List<Integer> bingoNumbers = new ArrayList<>();
        List<Integer> safetyNumbers = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        StringBuilder reasoning = new StringBuilder();

        // === CUSTOM RULE NUMBERS: Highest confidence (predefined patterns) ===
        if (!matchedRules.isEmpty()) {
            Set<Integer> ruleNumbers = new LinkedHashSet<>();

            for (MatchedRule rule : matchedRules) {
                ruleNumbers.addAll(rule.getSuggestedNumbers());
                reasoning.append(String.format("[RULE: %s] ", rule.getName()));
            }

            // Add rule-based numbers as bingo (highest priority)
            for (Integer number : ruleNumbers.stream().limit(6).toList()) {
                betItems.add(new BetItem(number, bingoStake, BetType.CUSTOM_RULE, "bingo"));
                bingoNumbers.add(number);
            }

            reasoning.append(String.format("Custom rules suggest: %s. ",
                    ruleNumbers.stream().limit(6).map(String::valueOf).collect(Collectors.joining(", "))));
        }

        // === BINGO NUMBERS: High-confidence from hot numbers ===
        if (!patterns.getHotNumbers().isEmpty()) {
            List<Integer> topHot = patterns.getHotNumbers().stream()
                    .filter(num -> !bingoNumbers.contains(num)) // Avoid duplicates from custom rules
                    .limit(5)
                    .toList();

            for (Integer number : topHot) {
                betItems.add(new BetItem(number, bingoStake, BetType.HOT_NUMBER, "bingo"));
                bingoNumbers.add(number);
            }

            if (!topHot.isEmpty()) {
                reasoning.append(String.format("Hot numbers (high confidence): %s. ", topHot));
            }
        }

        // === SAFETY NUMBERS: Backup coverage ===
        // Add neighbors of hot numbers
        if (!patterns.getHotNumbers().isEmpty()) {
            Set<Integer> neighborSet = new LinkedHashSet<>();
            for (Integer hotNum : patterns.getHotNumbers().stream().limit(2).toList()) {
                List<Integer> neighbors = patternAnalyzer.getNeighbors(hotNum, 2);
                neighborSet.addAll(neighbors);
            }

            // Remove numbers already in bingo list
            neighborSet.removeAll(bingoNumbers);

            List<Integer> safetyNeighbors = neighborSet.stream()
                    .limit(5)
                    .toList();

            for (Integer number : safetyNeighbors) {
                betItems.add(new BetItem(number, safetyStake, BetType.NEIGHBOR, "safety"));
                safetyNumbers.add(number);
            }

            reasoning.append(String.format("Neighbors (safety): %s. ", safetyNeighbors));
        }

        // Add some missing numbers as safety (contrarian strategy)
        if (!patterns.getMissingNumbers().isEmpty()) {
            List<Integer> missingToAdd = patterns.getMissingNumbers().stream()
                    .filter(num -> !bingoNumbers.contains(num) && !safetyNumbers.contains(num))
                    .limit(3)
                    .toList();

            for (Integer number : missingToAdd) {
                betItems.add(new BetItem(number, safetyStake, BetType.MISSING_NUMBER, "safety"));
                safetyNumbers.add(number);
            }

            if (!missingToAdd.isEmpty()) {
                reasoning.append(String.format("Missing numbers (contrarian): %s. ", missingToAdd));
            }
        }

        // Limit to max numbers
        if (betItems.size() > maxNumbers) {
            betItems = betItems.stream().limit(maxNumbers).toList();
        }

        // Calculate total stake
        BigDecimal totalStake = betItems.stream()
                .map(BetItem::getStake)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate recommended stake from discipline enforcer
        BigDecimal recommendedMaxStake = disciplineEnforcer.calculateRecommendedStake(session);

        // Determine confidence level
        String confidence = determineConfidence(patterns, betItems.size());

        // Add warnings if applicable
        if (totalStake.compareTo(recommendedMaxStake) > 0) {
            warnings.add("⚠️ Total stake exceeds recommended max (" + recommendedMaxStake + ")");
        }

        if (patterns.getHotNumbers().isEmpty()) {
            warnings.add("⚠️ No strong hot numbers detected - low pattern confidence");
            confidence = "LOW";
        }

        // Add section clustering info
        String hotSections = patterns.getDominantSection();
        if (hotSections != null && !hotSections.isEmpty()) {
            reasoning.append("Hot sections: ").append(hotSections).append(".");
        }

        log.info("Generated {} bet items (bingo: {}, safety: {}) with confidence: {}",
                betItems.size(), bingoNumbers.size(), safetyNumbers.size(), confidence);

        return SmartBetSuggestion.builder()
                .betItems(betItems)
                .totalStake(totalStake)
                .recommendedStake(recommendedMaxStake)
                .bingoNumbers(bingoNumbers)
                .safetyNumbers(safetyNumbers)
                .reasoning(reasoning.toString())
                .confidence(confidence)
                .warnings(warnings)
                .hotSections(hotSections)
                .matchedRules(matchedRules)
                .build();
    }

    /**
     * Determine confidence level based on pattern strength.
     */
    private String determineConfidence(PatternSuggestionResponse patterns, int betCount) {
        int hotCount = patterns.getHotNumbers().size();
        boolean hasSectionClustering = patterns.getDominantSection() != null;

        if (hotCount >= 5 && hasSectionClustering && betCount >= 10) {
            return "HIGH";
        } else if (hotCount >= 3 && betCount >= 5) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
