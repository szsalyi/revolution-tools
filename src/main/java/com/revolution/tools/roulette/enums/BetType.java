package com.revolution.tools.roulette.enums;

/**
 * Type/Source of roulette bet for data analysis.
 *
 * This enum tracks WHY a number was chosen, enabling future analysis of which
 * patterns/strategies produce better results.
 *
 * A single bet can contain numbers from MULTIPLE sources:
 * Example: Previous spin was 2
 * - CUSTOM_RULE: 2 → suggests 4, 22
 * - HOT_NUMBER: 14, 34 (appeared 5+ times recently)
 * - NEIGHBOR: 17, 32, 15 (neighbors of hot number 32)
 *
 * Bet would be: [4, 22, 14, 34, 17, 32, 15]
 * Sources:      [CUSTOM_RULE, CUSTOM_RULE, HOT_NUMBER, HOT_NUMBER, NEIGHBOR, NEIGHBOR, NEIGHBOR]
 */
public enum BetType {
    /**
     * Number suggested by custom pattern rules.
     * Example: Previous number 2 → suggests 4, 22 (double-digit pattern)
     */
    CUSTOM_RULE,

    /**
     * Hot number - appeared frequently in recent spins.
     * Example: Number appeared 3+ times in last 50 spins
     */
    HOT_NUMBER,

    /**
     * Cold/Missing number - hasn't appeared recently.
     * Example: Number not seen in last 100 spins
     */
    MISSING_NUMBER,

    /**
     * Wheel neighbor of a hot/custom number.
     * Example: 17 ± 2 neighbors on the wheel
     */
    NEIGHBOR,

    /**
     * Section clustering pattern.
     * Example: Multiple hits in Voisins du Zéro section
     */
    SECTION_PATTERN,

    /**
     * Double-digit pattern.
     * Example: 11, 22, 33 pattern detection
     */
    DOUBLE_DIGIT,

    /**
     * Repeating number from last spin.
     */
    REPEATING,

    // === Traditional Bet Types (for backward compatibility) ===

    /**
     * Single number straight bet (legacy)
     */
    STRAIGHT,

    /**
     * Multiple individual numbers (legacy)
     */
    MULTI_STRAIGHT,

    /**
     * Section bet: Voisins, Tiers, Orphelins (legacy)
     */
    SECTION,

    /**
     * Dozen bet: 1-12, 13-24, 25-36 (legacy)
     */
    DOZEN,

    /**
     * Column bet (legacy)
     */
    COLUMN,

    /**
     * Red/Black (legacy)
     */
    COLOR,

    /**
     * Even/Odd (legacy)
     */
    EVEN_ODD,

    /**
     * High/Low: 1-18/19-36 (legacy)
     */
    HIGH_LOW,

    /**
     * Generic pattern-based bet (legacy)
     */
    PATTERN_BASED,

    /**
     * Unknown/invalid source (parsing fallback)
     */
    UNKNOWN
}

