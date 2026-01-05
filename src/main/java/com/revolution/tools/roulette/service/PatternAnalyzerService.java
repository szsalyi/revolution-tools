package com.revolution.tools.roulette.service;

import com.revolution.tools.roulette.dto.response.PatternSuggestionResponse;

import java.util.List;

/**
 * Service for analyzing spin patterns and suggesting betting strategies.
 * Detects hot numbers, neighbors, missing numbers, and section clustering.
 */
public interface PatternAnalyzerService {

    /**
     * Analyze patterns for a session and suggest numbers to bet on.
     *
     * @param sessionId Session identifier
     * @return Pattern suggestions with detected patterns
     */
    PatternSuggestionResponse analyzePatterns(Long sessionId);

    /**
     * Get hot numbers (appeared frequently in recent spins).
     * Hot number: appeared 3+ times in last 50 spins.
     *
     * @param sessionId Session identifier
     * @param lookbackSpins Number of spins to analyze (default 50)
     * @param minFrequency Minimum frequency to be considered hot (default 3)
     * @return List of hot numbers
     */
    List<Integer> getHotNumbers(Long sessionId, Integer lookbackSpins, Integer minFrequency);

    /**
     * Get neighbors for a given number on the roulette wheel.
     * Neighbors: ±2 positions on the wheel.
     *
     * @param number Center number
     * @param distance Number of positions to include (default 2)
     * @return List of neighbor numbers
     */
    List<Integer> getNeighbors(Integer number, Integer distance);

    /**
     * Get missing numbers (haven't appeared in recent spins).
     * Missing: 6-8 numbers not in last 100 spins.
     *
     * @param sessionId Session identifier
     * @param lookbackSpins Number of spins to analyze (default 100)
     * @param maxMissing Maximum numbers to return (default 8)
     * @return List of missing numbers
     */
    List<Integer> getMissingNumbers(Long sessionId, Integer lookbackSpins, Integer maxMissing);

    /**
     * Detect section clustering (multiple spins in same wheel section).
     *
     * @param sessionId Session identifier
     * @param lookbackSpins Number of spins to analyze (default 20)
     * @return Dominant section if clustering detected, null otherwise
     */
    String detectSectionClustering(Long sessionId, Integer lookbackSpins);

    /**
     * Check if a bet matches the detected patterns.
     *
     * @param sessionId Session identifier
     * @param betNumbers Numbers being bet on
     * @return true if bet matches current patterns
     */
    boolean betMatchesPatterns(Long sessionId, List<Integer> betNumbers);
}
