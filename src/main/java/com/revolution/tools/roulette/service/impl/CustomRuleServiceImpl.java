package com.revolution.tools.roulette.service.impl;

import com.revolution.tools.roulette.dto.response.MatchedRule;
import com.revolution.tools.roulette.entity.CustomRule;
import com.revolution.tools.roulette.entity.RouletteSpin;
import com.revolution.tools.roulette.enums.RuleType;
import com.revolution.tools.roulette.repository.CustomRuleRepository;
import com.revolution.tools.roulette.repository.RouletteSpinRepository;
import com.revolution.tools.roulette.service.CustomRuleService;
import com.revolution.tools.roulette.service.PatternAnalyzerService;
import com.revolution.tools.roulette.util.RouletteCharacteristics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of CustomRuleService.
 * Evaluates custom betting rules against recent spins.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomRuleServiceImpl implements CustomRuleService {

    private final CustomRuleRepository ruleRepository;
    private final RouletteSpinRepository spinRepository;
    private final PatternAnalyzerService patternAnalyzer;

    @Override
    @Transactional(readOnly = true)
    public List<MatchedRule> evaluateRules(Long sessionId) {
        return evaluateRules(sessionId, 5); // Default: check last 5 spins
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchedRule> evaluateRules(Long sessionId, Integer lookbackSpins) {
        log.debug("Evaluating custom rules for session {} (lookback: {})", sessionId, lookbackSpins);

        // Get recent spins
        List<RouletteSpin> recentSpins = spinRepository.findLastNSpins(sessionId, lookbackSpins);
        if (recentSpins.isEmpty()) {
            log.debug("No spins found for session {}", sessionId);
            return new ArrayList<>();
        }

        // Get all enabled rules
        List<CustomRule> enabledRules = ruleRepository.findByEnabledTrue();
        log.debug("Found {} enabled rules", enabledRules.size());

        // Evaluate each rule
        List<MatchedRule> matchedRules = new ArrayList<>();
        for (CustomRule rule : enabledRules) {
            MatchedRule matched = evaluateRule(rule, recentSpins);
            if (matched != null) {
                matchedRules.add(matched);
                log.info("Rule matched: {} - suggests {}", rule.getName(), matched.getSuggestedNumbers());
            }
        }

        return matchedRules;
    }

    /**
     * Evaluate a single rule against recent spins.
     */
    private MatchedRule evaluateRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        return switch (rule.getRuleType()) {
            case ADJACENT -> evaluateAdjacentRule(rule, recentSpins);
            case PAIR -> evaluatePairRule(rule, recentSpins);
            case DELAYED_PAIR -> evaluateDelayedPairRule(rule, recentSpins);
            case GROUP_CORRELATION -> evaluateGroupCorrelationRule(rule, recentSpins);
            case SEQUENCE -> evaluateSequenceRule(rule, recentSpins);
            case HOT_STREAK -> evaluateHotStreakRule(rule, recentSpins);
            case COLD_NUMBER -> evaluateColdNumberRule(rule, recentSpins);
            case TIME_BASED -> evaluateTimeBasedRule(rule, recentSpins);
            case COLOR_ALTERNATION -> evaluateColorAlternationRule(rule, recentSpins);
            case SECTOR_BOUNCE -> evaluateSectorBounceRule(rule, recentSpins);
            case DOZEN_CYCLE -> evaluateDozenCycleRule(rule, recentSpins);
            case EVEN_ODD_PATTERN -> evaluateEvenOddPatternRule(rule, recentSpins);
            case REPEATING_DISTANCE -> evaluateRepeatingDistanceRule(rule, recentSpins);
            case MIRROR_NUMBERS -> evaluateMirrorNumbersRule(rule, recentSpins);
            case GAP_PATTERN -> evaluateGapPatternRule(rule, recentSpins);
            case STREAK_BREAKER -> evaluateStreakBreakerRule(rule, recentSpins);
            default -> {
                log.warn("Unsupported rule type: {}", rule.getRuleType());
                yield null;
            }
        };
    }

    /**
     * Evaluate ADJACENT rule (e.g., if 32, suggest ±2: 30, 34).
     */
    private MatchedRule evaluateAdjacentRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.isEmpty()) {
            return null;
        }

        // Get last spin number
        Integer lastNumber = recentSpins.get(0).getSpinNumber();

        // Parse trigger numbers
        List<Integer> triggerNumbers = parseNumbers(rule.getTriggerNumbers());

        // Check if last number matches any trigger
        if (!triggerNumbers.contains(lastNumber)) {
            return null; // Rule doesn't match
        }

        // Rule matched! Return suggested numbers
        List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());

        return MatchedRule.builder()
                .ruleId(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .triggerNumbers(List.of(lastNumber))
                .suggestedNumbers(suggestedNumbers)
                .confidence(rule.getConfidence())
                .hitRate(rule.getHitRate())
                .reason(String.format("Last number %d matches trigger - suggests adjacent numbers", lastNumber))
                .build();
    }

    /**
     * Evaluate PAIR rule (e.g., if 30, suggest 3; if 3, suggest 30).
     */
    private MatchedRule evaluatePairRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.isEmpty()) {
            return null;
        }

        // Get last spin number
        Integer lastNumber = recentSpins.get(0).getSpinNumber();

        // Parse trigger numbers
        List<Integer> triggerNumbers = parseNumbers(rule.getTriggerNumbers());

        // Check if last number matches any trigger
        if (!triggerNumbers.contains(lastNumber)) {
            return null; // Rule doesn't match
        }

        // Rule matched! Return suggested numbers
        List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());

        return MatchedRule.builder()
                .ruleId(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .triggerNumbers(List.of(lastNumber))
                .suggestedNumbers(suggestedNumbers)
                .confidence(rule.getConfidence())
                .hitRate(rule.getHitRate())
                .reason(String.format("Number %d suggests paired number(s): %s",
                        lastNumber, suggestedNumbers.stream().map(String::valueOf).collect(Collectors.joining(", "))))
                .build();
    }

    /**
     * Evaluate DELAYED_PAIR rule (e.g., 30 → [any] → suggest 3).
     * Pattern triggers after 1-spin delay.
     */
    private MatchedRule evaluateDelayedPairRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.size() < 2) {
            return null; // Need at least 2 spins
        }

        // Get number from 2 spins ago (spin N-2)
        Integer twoSpinsAgo = recentSpins.get(1).getSpinNumber();
        Integer lastNumber = recentSpins.get(0).getSpinNumber();

        // Parse trigger numbers
        List<Integer> triggerNumbers = parseNumbers(rule.getTriggerNumbers());

        // Check if number 2 spins ago matches any trigger
        if (!triggerNumbers.contains(twoSpinsAgo)) {
            return null; // Rule doesn't match
        }

        // Rule matched! The pattern is: twoSpinsAgo → lastNumber → [suggest now]
        List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());

        return MatchedRule.builder()
                .ruleId(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .triggerNumbers(List.of(twoSpinsAgo, lastNumber))
                .suggestedNumbers(suggestedNumbers)
                .confidence(rule.getConfidence())
                .hitRate(rule.getHitRate())
                .reason(String.format("Delayed pattern: %d → %d → suggests %s (1-spin delay)",
                        twoSpinsAgo, lastNumber, suggestedNumbers.stream().map(String::valueOf).collect(Collectors.joining(", "))))
                .build();
    }

    /**
     * Evaluate GROUP_CORRELATION rule (interconnected number group).
     * Example: If any of [31, 33, 13, 11] appears, suggest the others + neighbors.
     */
    private MatchedRule evaluateGroupCorrelationRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.isEmpty()) {
            return null;
        }

        // Get last spin number
        Integer lastNumber = recentSpins.get(0).getSpinNumber();

        // Parse the group (trigger numbers contains all correlated numbers)
        List<Integer> groupNumbers = parseNumbers(rule.getTriggerNumbers());

        // Check if last number is in the group
        if (!groupNumbers.contains(lastNumber)) {
            return null; // Rule doesn't match
        }

        // Build suggestions: other group members + neighbors of all group members
        Set<Integer> suggestedSet = new LinkedHashSet<>();

        // Add other group members (exclude the one that just hit)
        for (Integer groupNum : groupNumbers) {
            if (!groupNum.equals(lastNumber)) {
                suggestedSet.add(groupNum);
            }
        }

        // Add neighbors of ALL group members (including the one that hit)
        for (Integer groupNum : groupNumbers) {
            List<Integer> neighbors = patternAnalyzer.getNeighbors(groupNum, 2);
            suggestedSet.addAll(neighbors);
        }

        // Remove the number that just hit
        suggestedSet.remove(lastNumber);

        // If rule has pre-specified suggested numbers, use those instead
        List<Integer> ruleSuggested = parseNumbers(rule.getSuggestedNumbers());
        List<Integer> suggestedNumbers;
        if (!ruleSuggested.isEmpty()) {
            suggestedNumbers = ruleSuggested;
        } else {
            suggestedNumbers = new ArrayList<>(suggestedSet);
        }

        if (suggestedNumbers.isEmpty()) {
            return null;
        }

        return MatchedRule.builder()
                .ruleId(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .triggerNumbers(List.of(lastNumber))
                .suggestedNumbers(suggestedNumbers.stream().limit(15).toList())
                .confidence(rule.getConfidence())
                .hitRate(rule.getHitRate())
                .reason(String.format("Group correlation: %d triggered → suggests group members %s + neighbors",
                        lastNumber, groupNumbers.stream().filter(n -> !n.equals(lastNumber))
                                .map(String::valueOf).collect(Collectors.joining(", "))))
                .build();
    }

    /**
     * Evaluate SEQUENCE rule (last N spins match pattern).
     */
    private MatchedRule evaluateSequenceRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        // Parse trigger sequence
        List<Integer> triggerSequence = parseNumbers(rule.getTriggerNumbers());

        if (recentSpins.size() < triggerSequence.size()) {
            return null; // Not enough spins
        }

        // Extract last N numbers
        List<Integer> lastNumbers = recentSpins.stream()
                .limit(triggerSequence.size())
                .map(RouletteSpin::getSpinNumber)
                .toList();

        // Check if sequence matches
        if (!lastNumbers.equals(triggerSequence)) {
            return null; // Sequence doesn't match
        }

        // Rule matched!
        List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());

        return MatchedRule.builder()
                .ruleId(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .triggerNumbers(lastNumbers)
                .suggestedNumbers(suggestedNumbers)
                .confidence(rule.getConfidence())
                .hitRate(rule.getHitRate())
                .reason(String.format("Sequence %s detected", lastNumbers))
                .build();
    }

    /**
     * Evaluate HOT_STREAK rule (number appeared K times recently).
     */
    private MatchedRule evaluateHotStreakRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        // Parse trigger numbers (numbers to watch for hot streaks)
        List<Integer> triggerNumbers = parseNumbers(rule.getTriggerNumbers());

        for (Integer number : triggerNumbers) {
            long count = recentSpins.stream()
                    .filter(spin -> spin.getSpinNumber().equals(number))
                    .count();

            // If number appeared 2+ times in recent spins, it's a hot streak
            if (count >= 2) {
                List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());

                return MatchedRule.builder()
                        .ruleId(rule.getId())
                        .name(rule.getName())
                        .description(rule.getDescription())
                        .ruleType(rule.getRuleType())
                        .triggerNumbers(List.of(number))
                        .suggestedNumbers(suggestedNumbers)
                        .confidence(rule.getConfidence())
                        .hitRate(rule.getHitRate())
                        .reason(String.format("Number %d appeared %d times in last %d spins (hot streak)",
                                number, count, recentSpins.size()))
                        .build();
            }
        }

        return null; // No hot streak detected
    }

    /**
     * Evaluate COLD_NUMBER rule (number hasn't appeared in N spins).
     */
    private MatchedRule evaluateColdNumberRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        // Parse trigger numbers (numbers to watch for cold patterns)
        List<Integer> triggerNumbers = parseNumbers(rule.getTriggerNumbers());

        List<Integer> recentSpinNumbers = recentSpins.stream()
                .map(RouletteSpin::getSpinNumber)
                .toList();

        // Check if any trigger number is missing from recent spins
        for (Integer number : triggerNumbers) {
            if (!recentSpinNumbers.contains(number)) {
                List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());

                return MatchedRule.builder()
                        .ruleId(rule.getId())
                        .name(rule.getName())
                        .description(rule.getDescription())
                        .ruleType(rule.getRuleType())
                        .triggerNumbers(List.of(number))
                        .suggestedNumbers(suggestedNumbers)
                        .confidence(rule.getConfidence())
                        .hitRate(rule.getHitRate())
                        .reason(String.format("Number %d hasn't appeared in last %d spins (cold number)",
                                number, recentSpins.size()))
                        .build();
            }
        }

        return null; // No cold numbers detected
    }

    /**
     * Evaluate TIME_BASED rule (after N spins, suggest numbers).
     */
    private MatchedRule evaluateTimeBasedRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        int totalSpins = recentSpins.size();

        // Parse config to get spin threshold (default 50)
        int spinThreshold = 50; // TODO: Parse from configJson

        if (totalSpins >= spinThreshold) {
            List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());

            return MatchedRule.builder()
                    .ruleId(rule.getId())
                    .name(rule.getName())
                    .description(rule.getDescription())
                    .ruleType(rule.getRuleType())
                    .triggerNumbers(List.of())
                    .suggestedNumbers(suggestedNumbers)
                    .confidence(rule.getConfidence())
                    .hitRate(rule.getHitRate())
                    .reason(String.format("Session reached %d spins threshold", totalSpins))
                    .build();
        }

        return null;
    }

    /**
     * Evaluate COLOR_ALTERNATION rule (3+ same color → suggest opposite).
     */
    private MatchedRule evaluateColorAlternationRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.size() < 3) {
            return null;
        }

        // Check last 3 spins for same color
        List<Integer> last3 = recentSpins.stream().limit(3).map(RouletteSpin::getSpinNumber).toList();

        boolean allRed = last3.stream().allMatch(RouletteCharacteristics::isRed);
        boolean allBlack = last3.stream().allMatch(RouletteCharacteristics::isBlack);

        if (allRed || allBlack) {
            String color = allRed ? "red" : "black";
            List<Integer> oppositeNumbers = allRed ?
                RouletteCharacteristics.getAllBlackNumbers() :
                RouletteCharacteristics.getAllRedNumbers();

            // Use suggested numbers from rule, or default to all opposite color
            List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());
            if (suggestedNumbers.isEmpty()) {
                suggestedNumbers = oppositeNumbers;
            }

            return MatchedRule.builder()
                    .ruleId(rule.getId())
                    .name(rule.getName())
                    .description(rule.getDescription())
                    .ruleType(rule.getRuleType())
                    .triggerNumbers(last3)
                    .suggestedNumbers(suggestedNumbers)
                    .confidence(rule.getConfidence())
                    .hitRate(rule.getHitRate())
                    .reason(String.format("Last 3 spins all %s - suggesting opposite color", color))
                    .build();
        }

        return null;
    }

    /**
     * Evaluate SECTOR_BOUNCE rule (sector A → suggest sector B).
     */
    private MatchedRule evaluateSectorBounceRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.isEmpty()) {
            return null;
        }

        Integer lastNumber = recentSpins.get(0).getSpinNumber();
        Integer lastSector = RouletteCharacteristics.getSector(lastNumber);

        if (lastSector == null) {
            return null;
        }

        // Parse trigger sector from config
        List<Integer> triggerSectors = parseNumbers(rule.getTriggerNumbers());

        if (triggerSectors.contains(lastSector)) {
            List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());

            return MatchedRule.builder()
                    .ruleId(rule.getId())
                    .name(rule.getName())
                    .description(rule.getDescription())
                    .ruleType(rule.getRuleType())
                    .triggerNumbers(List.of(lastNumber))
                    .suggestedNumbers(suggestedNumbers)
                    .confidence(rule.getConfidence())
                    .hitRate(rule.getHitRate())
                    .reason(String.format("Last number %d in sector %d - bounce pattern", lastNumber, lastSector))
                    .build();
        }

        return null;
    }

    /**
     * Evaluate DOZEN_CYCLE rule (dozen rotation pattern).
     */
    private MatchedRule evaluateDozenCycleRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.size() < 3) {
            return null;
        }

        // Count dozen hits in last N spins
        List<Integer> last5Numbers = recentSpins.stream().limit(5).map(RouletteSpin::getSpinNumber).toList();

        int dozen1Count = (int) last5Numbers.stream().filter(n -> RouletteCharacteristics.getDozen(n) != null && RouletteCharacteristics.getDozen(n) == 1).count();
        int dozen2Count = (int) last5Numbers.stream().filter(n -> RouletteCharacteristics.getDozen(n) != null && RouletteCharacteristics.getDozen(n) == 2).count();
        int dozen3Count = (int) last5Numbers.stream().filter(n -> RouletteCharacteristics.getDozen(n) != null && RouletteCharacteristics.getDozen(n) == 3).count();

        // If one dozen hit 3+ times, suggest other dozens
        Integer hotDozen = null;
        if (dozen1Count >= 3) hotDozen = 1;
        else if (dozen2Count >= 3) hotDozen = 2;
        else if (dozen3Count >= 3) hotDozen = 3;

        if (hotDozen != null) {
            // Suggest other dozens
            List<Integer> suggestedNumbers = new ArrayList<>();
            for (int dozen = 1; dozen <= 3; dozen++) {
                if (dozen != hotDozen) {
                    suggestedNumbers.addAll(RouletteCharacteristics.getDozenNumbers(dozen));
                }
            }

            // Or use rule's suggested numbers
            List<Integer> ruleSuggested = parseNumbers(rule.getSuggestedNumbers());
            if (!ruleSuggested.isEmpty()) {
                suggestedNumbers = ruleSuggested;
            }

            return MatchedRule.builder()
                    .ruleId(rule.getId())
                    .name(rule.getName())
                    .description(rule.getDescription())
                    .ruleType(rule.getRuleType())
                    .triggerNumbers(last5Numbers)
                    .suggestedNumbers(suggestedNumbers.stream().limit(12).toList())
                    .confidence(rule.getConfidence())
                    .hitRate(rule.getHitRate())
                    .reason(String.format("Dozen %d hit %d times in last 5 spins - cycle pattern", hotDozen,
                            hotDozen == 1 ? dozen1Count : (hotDozen == 2 ? dozen2Count : dozen3Count)))
                    .build();
        }

        return null;
    }

    /**
     * Evaluate EVEN_ODD_PATTERN rule.
     */
    private MatchedRule evaluateEvenOddPatternRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.size() < 3) {
            return null;
        }

        List<Integer> last3 = recentSpins.stream().limit(3).map(RouletteSpin::getSpinNumber).toList();

        boolean allEven = last3.stream().allMatch(RouletteCharacteristics::isEven);
        boolean allOdd = last3.stream().allMatch(RouletteCharacteristics::isOdd);

        if (allEven || allOdd) {
            String parity = allEven ? "even" : "odd";
            List<Integer> oppositeNumbers = allEven ?
                RouletteCharacteristics.getAllOddNumbers() :
                RouletteCharacteristics.getAllEvenNumbers();

            List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());
            if (suggestedNumbers.isEmpty()) {
                suggestedNumbers = oppositeNumbers;
            }

            return MatchedRule.builder()
                    .ruleId(rule.getId())
                    .name(rule.getName())
                    .description(rule.getDescription())
                    .ruleType(rule.getRuleType())
                    .triggerNumbers(last3)
                    .suggestedNumbers(suggestedNumbers)
                    .confidence(rule.getConfidence())
                    .hitRate(rule.getHitRate())
                    .reason(String.format("Last 3 spins all %s - suggesting opposite parity", parity))
                    .build();
        }

        return null;
    }

    /**
     * Evaluate REPEATING_DISTANCE rule.
     */
    private MatchedRule evaluateRepeatingDistanceRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.size() < 2) {
            return null;
        }

        Integer last = recentSpins.get(0).getSpinNumber();
        Integer secondLast = recentSpins.get(1).getSpinNumber();

        int distance = Math.abs(last - secondLast);

        // Suggest numbers at same distance
        List<Integer> suggestedNumbers = new ArrayList<>();
        int next1 = last + distance;
        int next2 = last - distance;

        if (next1 >= 0 && next1 <= 36) suggestedNumbers.add(next1);
        if (next2 >= 0 && next2 <= 36) suggestedNumbers.add(next2);

        if (!suggestedNumbers.isEmpty()) {
            return MatchedRule.builder()
                    .ruleId(rule.getId())
                    .name(rule.getName())
                    .description(rule.getDescription())
                    .ruleType(rule.getRuleType())
                    .triggerNumbers(List.of(secondLast, last))
                    .suggestedNumbers(suggestedNumbers)
                    .confidence(rule.getConfidence())
                    .hitRate(rule.getHitRate())
                    .reason(String.format("Distance pattern detected: %d → %d (distance %d)", secondLast, last, distance))
                    .build();
        }

        return null;
    }

    /**
     * Evaluate MIRROR_NUMBERS rule.
     */
    private MatchedRule evaluateMirrorNumbersRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.isEmpty()) {
            return null;
        }

        Integer lastNumber = recentSpins.get(0).getSpinNumber();
        Integer mirrorNumber = RouletteCharacteristics.getMirrorNumber(lastNumber);

        if (mirrorNumber != null) {
            return MatchedRule.builder()
                    .ruleId(rule.getId())
                    .name(rule.getName())
                    .description(rule.getDescription())
                    .ruleType(rule.getRuleType())
                    .triggerNumbers(List.of(lastNumber))
                    .suggestedNumbers(List.of(mirrorNumber))
                    .confidence(rule.getConfidence())
                    .hitRate(rule.getHitRate())
                    .reason(String.format("Last number %d - mirror number %d (opposite wheel side)", lastNumber, mirrorNumber))
                    .build();
        }

        return null;
    }

    /**
     * Evaluate GAP_PATTERN rule.
     */
    private MatchedRule evaluateGapPatternRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        // Parse gap size from config (default 20)
        int gapSize = 20; // TODO: Parse from configJson

        if (recentSpins.size() < gapSize + 1) {
            return null;
        }

        // Get numbers that appeared before the gap
        List<Integer> beforeGap = recentSpins.stream()
                .skip(gapSize)
                .map(RouletteSpin::getSpinNumber)
                .toList();

        // Get numbers during the gap
        List<Integer> duringGap = recentSpins.stream()
                .limit(gapSize)
                .map(RouletteSpin::getSpinNumber)
                .toList();

        // Find numbers that appeared before but not during gap
        List<Integer> gapNumbers = beforeGap.stream()
                .distinct()
                .filter(num -> !duringGap.contains(num))
                .toList();

        if (!gapNumbers.isEmpty()) {
            List<Integer> suggestedNumbers = parseNumbers(rule.getSuggestedNumbers());
            if (suggestedNumbers.isEmpty()) {
                suggestedNumbers = gapNumbers.stream().limit(5).toList();
            }

            return MatchedRule.builder()
                    .ruleId(rule.getId())
                    .name(rule.getName())
                    .description(rule.getDescription())
                    .ruleType(rule.getRuleType())
                    .triggerNumbers(gapNumbers)
                    .suggestedNumbers(suggestedNumbers)
                    .confidence(rule.getConfidence())
                    .hitRate(rule.getHitRate())
                    .reason(String.format("Numbers %s haven't appeared in last %d spins (gap pattern)", gapNumbers, gapSize))
                    .build();
        }

        return null;
    }

    /**
     * Evaluate STREAK_BREAKER rule.
     */
    private MatchedRule evaluateStreakBreakerRule(CustomRule rule, List<RouletteSpin> recentSpins) {
        if (recentSpins.size() < 5) {
            return null;
        }

        List<Integer> last5 = recentSpins.stream().limit(5).map(RouletteSpin::getSpinNumber).toList();

        // Find numbers that appeared 2+ times
        for (Integer number : last5) {
            long count = last5.stream().filter(n -> n.equals(number)).count();

            if (count >= 2) {
                // Suggest opposite characteristics
                List<Integer> suggestedNumbers = new ArrayList<>();
                suggestedNumbers.addAll(RouletteCharacteristics.getOppositeColorNumbers(number));
                suggestedNumbers.addAll(RouletteCharacteristics.getOppositeParity(number));

                // Remove duplicates and limit
                suggestedNumbers = suggestedNumbers.stream().distinct().limit(10).toList();

                // Or use rule's suggestions
                List<Integer> ruleSuggested = parseNumbers(rule.getSuggestedNumbers());
                if (!ruleSuggested.isEmpty()) {
                    suggestedNumbers = ruleSuggested;
                }

                return MatchedRule.builder()
                        .ruleId(rule.getId())
                        .name(rule.getName())
                        .description(rule.getDescription())
                        .ruleType(rule.getRuleType())
                        .triggerNumbers(List.of(number))
                        .suggestedNumbers(suggestedNumbers)
                        .confidence(rule.getConfidence())
                        .hitRate(rule.getHitRate())
                        .reason(String.format("Number %d hit %d times in last 5 spins - suggesting opposite characteristics", number, count))
                        .build();
            }
        }

        return null;
    }

    @Override
    @Transactional
    public CustomRule saveRule(CustomRule rule) {
        log.info("Saving custom rule: {}", rule.getName());
        return ruleRepository.save(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomRule> getAllEnabledRules() {
        return ruleRepository.findByEnabledTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomRule getRuleById(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));
    }

    @Override
    @Transactional
    public CustomRule toggleRule(Long id, Boolean enabled) {
        CustomRule rule = getRuleById(id);
        rule.setEnabled(enabled);
        log.info("Toggled rule {} to enabled={}", rule.getName(), enabled);
        return ruleRepository.save(rule);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        log.info("Deleting rule ID: {}", id);
        ruleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void initializeDefaultRules() {
        log.info("Initializing default custom rules...");

        // Rule 1: If 32, suggest ±2 (30, 34)
        createDefaultRuleIfNotExists(
                "32 Adjacent ±2",
                "If last number is 32, bet on adjacent numbers 30 and 34",
                RuleType.ADJACENT,
                "32",
                "30,34",
                75
        );

        // Rule 2: If 30, suggest 3 (and vice versa)
        createDefaultRuleIfNotExists(
                "30-3 Pair",
                "If 30 appears, bet on 3 (often follows)",
                RuleType.PAIR,
                "30",
                "3",
                70
        );

        createDefaultRuleIfNotExists(
                "3-30 Pair",
                "If 3 appears, bet on 30 (often follows)",
                RuleType.PAIR,
                "3",
                "30",
                70
        );

        // Rule 3: Delayed 30→3 Pair (30 → any → 3)
        createDefaultRuleIfNotExists(
                "30→3 Delayed",
                "If 30 appeared 2 spins ago, bet on 3 (1-spin delay pattern)",
                RuleType.DELAYED_PAIR,
                "30",
                "3",
                75
        );

        // Rule 4: Delayed 3→30 Pair (3 → any → 30)
        createDefaultRuleIfNotExists(
                "3→30 Delayed",
                "If 3 appeared 2 spins ago, bet on 30 (1-spin delay pattern)",
                RuleType.DELAYED_PAIR,
                "3",
                "30",
                75
        );

        // Rule 5: General adjacent rule for any number ending in 2
        createDefaultRuleIfNotExists(
                "Numbers ending in 2 - Adjacent",
                "Numbers ending in 2 often have adjacent hits",
                RuleType.ADJACENT,
                "2,12,22,32",
                "0,4,10,14,20,24,30,34",
                65
        );

        // Rule 6: Group correlation [31, 33, 13, 11]
        createDefaultRuleIfNotExists(
                "Group [31,33,13,11] Correlation",
                "If any of [31,33,13,11] appears, others in group + neighbors likely follow",
                RuleType.GROUP_CORRELATION,
                "31,33,13,11",
                "",
                80
        );

        log.info("Default rules initialization complete");
    }

    /**
     * Create a default rule if it doesn't already exist.
     */
    private void createDefaultRuleIfNotExists(String name, String description, RuleType ruleType,
                                              String triggerNumbers, String suggestedNumbers, Integer confidence) {
        if (ruleRepository.findByName(name).isEmpty()) {
            CustomRule rule = CustomRule.builder()
                    .name(name)
                    .description(description)
                    .ruleType(ruleType)
                    .triggerNumbers(triggerNumbers)
                    .suggestedNumbers(suggestedNumbers)
                    .confidence(confidence)
                    .enabled(true)
                    .build();
            ruleRepository.save(rule);
            log.info("Created default rule: {}", name);
        } else {
            log.debug("Rule already exists: {}", name);
        }
    }

    /**
     * Parse CSV string to list of integers.
     */
    private List<Integer> parseNumbers(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
