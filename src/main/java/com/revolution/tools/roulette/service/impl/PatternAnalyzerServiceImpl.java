package com.revolution.tools.roulette.service.impl;

import com.revolution.tools.roulette.dto.response.PatternSuggestionResponse;
import com.revolution.tools.roulette.entity.RouletteSpin;
import com.revolution.tools.roulette.enums.PatternType;
import com.revolution.tools.roulette.enums.RouletteSection;
import com.revolution.tools.roulette.repository.RouletteSessionRepository;
import com.revolution.tools.roulette.repository.RouletteSpinRepository;
import com.revolution.tools.roulette.service.PatternAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static java.util.stream.Collectors.*;

/**
 * Implementation of PatternAnalyzerService for detecting betting patterns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatternAnalyzerServiceImpl implements PatternAnalyzerService {

    private final RouletteSpinRepository spinRepository;
    private final RouletteSessionRepository sessionRepository;

    // European roulette wheel order (clockwise)
    private static final int[] WHEEL_ORDER = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
            5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };

    @Override
    @Transactional(readOnly = true)
    public PatternSuggestionResponse analyzePatterns(Long sessionId) {
        log.debug("Analyzing patterns for session {}", sessionId);

        List<Integer> hotNumbers = getHotNumbers(sessionId, 15, 3);
        List<Integer> missingNumbers = getMissingNumbers(sessionId, 15, 8);
        String dominantSection = detectSectionClustering(sessionId, 20);

        List<Integer> suggestedNumbers = new ArrayList<>();
        List<String> detectedPatterns = new ArrayList<>();

        // Suggest hot numbers
        if (!hotNumbers.isEmpty()) {
            suggestedNumbers.addAll(hotNumbers);
            detectedPatterns.add("HOT_NUMBERS: " + hotNumbers);
        }

        // Suggest neighbors of hot numbers
        if (!hotNumbers.isEmpty()) {
            List<Integer> neighbors = hotNumbers.stream()
                    .flatMap(num -> getNeighbors(num, 2).stream())
                    .distinct()
                    .collect( toList());
            suggestedNumbers.addAll(neighbors);
            detectedPatterns.add("NEIGHBORS: " + neighbors);
        }

        // Suggest missing numbers (contrarian strategy)
        if (!missingNumbers.isEmpty()) {
            detectedPatterns.add("MISSING_NUMBERS: " + missingNumbers);
        }

        // Suggest section clustering
        if (dominantSection != null) {
            detectedPatterns.add("SECTION_CLUSTERING: " + dominantSection);
        }

        // Remove duplicates and limit to 15 suggestions
        suggestedNumbers = suggestedNumbers.stream()
                .distinct()
                .limit(15)
                .sorted()
                .collect( toList());

        return PatternSuggestionResponse.builder()
                .sessionId(sessionId.toString())
                .hotNumbers(hotNumbers)
                .missingNumbers(missingNumbers)
                .suggestedNumbers(suggestedNumbers)
                .dominantSection(dominantSection)
                .detectedPatterns(detectedPatterns)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getHotNumbers(Long sessionId, Integer lookbackSpins, Integer minFrequency) {
        List<RouletteSpin> recentSpins = spinRepository.findLastNSpins(sessionId, lookbackSpins);

        // Count frequency of each number
        Map<Integer, Long> frequencyMap = recentSpins.stream()
                .collect( groupingBy(RouletteSpin::getSpinNumber, counting()));

        // Filter by minimum frequency and sort by frequency descending
        return frequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() >= minFrequency)
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)
                .collect( toList());
    }

    @Override
    public List<Integer> getNeighbors(Integer number, Integer distance) {
        List<Integer> neighbors = new ArrayList<>();

        // Find position of number on wheel
        int position = -1;
        for (int i = 0; i < WHEEL_ORDER.length; i++) {
            if (WHEEL_ORDER[i] == number) {
                position = i;
                break;
            }
        }

        if (position == -1) {
            log.warn("Number {} not found on wheel", number);
            return neighbors;
        }

        // Get neighbors within distance
        for (int i = -distance; i <= distance; i++) {
            if (i == 0) continue; // Skip the number itself

            int neighborPosition = (position + i + WHEEL_ORDER.length) % WHEEL_ORDER.length;
            neighbors.add(WHEEL_ORDER[neighborPosition]);
        }

        return neighbors;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getMissingNumbers(Long sessionId, Integer lookbackSpins, Integer maxMissing) {
        List<RouletteSpin> recentSpins = spinRepository.findLastNSpins(sessionId, lookbackSpins);

        // Get all numbers that appeared
        Set<Integer> appearedNumbers = recentSpins.stream()
                .map(RouletteSpin::getSpinNumber)
                .collect( toSet());

        // Find numbers that didn't appear (0-36)
        List<Integer> missingNumbers = IntStream.rangeClosed(0, 36)
                .filter(num -> !appearedNumbers.contains(num))
                .boxed()
                .collect( toList());

        // Randomly select up to maxMissing numbers
        Collections.shuffle(missingNumbers);
        return missingNumbers.stream()
                .limit(maxMissing)
                .sorted()
                .toList();
    }

    /**
     * Represents a dynamic wheel section with consecutive numbers.
     */
    private static class WheelSection {
        String name;
        List<Integer> numbers;
        int startPosition;
        long hitCount;
        double hitRate;

        WheelSection(int startPos, int size) {
            this.startPosition = startPos;
            this.numbers = new ArrayList<>();

            // Build consecutive section from wheel
            for (int i = 0; i < size; i++) {
                int position = (startPos + i) % WHEEL_ORDER.length;
                numbers.add(WHEEL_ORDER[position]);
            }

            // Create name from first and last numbers
            this.name = numbers.get(0) + "-" + numbers.get(numbers.size() - 1);
        }

        void calculateHitRate(List<Integer> spinNumbers, long totalSpins) {
            this.hitCount = spinNumbers.stream()
                    .filter(numbers::contains)
                    .count();
            this.hitRate = (hitCount * 100.0) / totalSpins;
        }

        @Override
        public String toString() {
            return name + ":" + String.format("%.1f", hitRate) + "%";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String detectSectionClustering(Long sessionId, Integer lookbackSpins) {
        List<RouletteSpin> recentSpins = spinRepository.findLastNSpins(sessionId, lookbackSpins);

        if (recentSpins.size() < 10) {
            return null; // Not enough data
        }

        // Get all spin numbers
        List<Integer> spinNumbers = recentSpins.stream()
                .map(RouletteSpin::getSpinNumber)
                .toList();

        long totalSpins = recentSpins.size();

        // === DYNAMIC SECTION DETECTION ===
        // Analyze all possible 8-number consecutive wheel sections
        List<WheelSection> allSections = new ArrayList<>();
        for (int startPos = 0; startPos < WHEEL_ORDER.length; startPos++) {
            WheelSection section = new WheelSection(startPos, 8);
            section.calculateHitRate(spinNumbers, totalSpins);
            allSections.add(section);
        }

        // Sort sections by hit rate descending
        allSections.sort((s1, s2) -> Double.compare(s2.hitRate, s1.hitRate));

        // Find hot sections (>25% hit rate)
        List<WheelSection> hotSections = allSections.stream()
                .filter(s -> s.hitRate > 25.0)
                .limit(5) // Max 5 hot sections
                .toList();

        if (!hotSections.isEmpty()) {
            log.info("Dynamic hot wheel sections detected:");
            for (WheelSection section : hotSections) {
                log.info("  Section {} (numbers {}): {}% hit rate ({}/{} spins)",
                        section.name, section.numbers,
                        String.format("%.1f", section.hitRate),
                        section.hitCount, totalSpins);
            }
        }

        // Detect paired hot sections (2+ sections above threshold)
        if (hotSections.size() >= 2) {
            // Check if sections overlap (avoid counting same numbers twice)
            List<WheelSection> nonOverlappingSections = filterNonOverlappingSections(hotSections);

            if (nonOverlappingSections.size() >= 2) {
                String pairedSections = nonOverlappingSections.stream()
                        .limit(3) // Max 3 sections in description
                        .map(WheelSection::toString)
                        .collect(Collectors.joining(" + "));

                log.info("PAIRED HOT SECTIONS detected: {}", pairedSections);
                return pairedSections;
            }
        }

        // Single dominant section (>40% hit rate)
        if (!hotSections.isEmpty() && hotSections.get(0).hitRate > 40.0) {
            WheelSection dominant = hotSections.get(0);
            log.info("Dominant section clustering: {} with {}% hit rate (numbers: {})",
                    dominant.name, String.format("%.1f", dominant.hitRate), dominant.numbers);
            return dominant.toString();
        }

        // Moderate hot section (>30% hit rate)
        if (!hotSections.isEmpty() && hotSections.get(0).hitRate > 30.0) {
            WheelSection hot = hotSections.get(0);
            log.info("Hot section detected: {} with {}% hit rate (numbers: {})",
                    hot.name, String.format("%.1f", hot.hitRate), hot.numbers);
            return hot.toString();
        }

        // Fallback: Check RouletteSection enum (zero/red/black clustering)
        Map<RouletteSection, Long> sectionCounts = recentSpins.stream()
                .filter(spin -> spin.getSection() != null)
                .collect( groupingBy(RouletteSpin::getSection, counting()));

        for (Map.Entry<RouletteSection, Long> entry : sectionCounts.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalSpins;
            if (percentage > 40.0) {
                log.info("Section clustering detected: {} appeared in {}% of last {} spins",
                        entry.getKey(), String.format("%.1f", percentage), lookbackSpins);
                return entry.getKey().name();
            }
        }

        return null;
    }

    /**
     * Filter out overlapping sections to avoid counting same numbers multiple times.
     * Keeps sections with highest hit rates and minimal overlap.
     */
    private List<WheelSection> filterNonOverlappingSections(List<WheelSection> sections) {
        List<WheelSection> filtered = new ArrayList<>();
        Set<Integer> usedNumbers = new HashSet<>();

        for (WheelSection section : sections) {
            // Check how many numbers overlap with already selected sections
            long overlapCount = section.numbers.stream()
                    .filter(usedNumbers::contains)
                    .count();

            // Allow section if less than 50% overlap
            if (overlapCount < section.numbers.size() / 2) {
                filtered.add(section);
                usedNumbers.addAll(section.numbers);
            }
        }

        return filtered;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean betMatchesPatterns(Long sessionId, List<Integer> betNumbers) {
        if (betNumbers == null || betNumbers.isEmpty()) {
            return false;
        }

        // Get current pattern suggestions
        PatternSuggestionResponse patterns = analyzePatterns(sessionId);

        // Check if at least 50% of bet numbers match suggested numbers
        long matchCount = betNumbers.stream()
                .filter(num -> patterns.getSuggestedNumbers().contains(num))
                .count();

        double matchPercentage = (matchCount * 100.0) / betNumbers.size();

        log.debug("Bet match percentage: {}% ({}/{} numbers)",
                String.format("%.1f", matchPercentage), matchCount, betNumbers.size());

        return matchPercentage >= 50.0;
    }
}
