package com.revolution.tools.roulette.enums;

/**
 * Types of custom betting rules.
 */
public enum RuleType {

    /**
     * Adjacent number rule.
     * If last number is X, suggest X±N (adjacent on wheel or numeric).
     * Example: If 32, suggest 30, 34 (±2)
     */
    ADJACENT,

    /**
     * Number pair rule.
     * If number X appears, suggest number Y (observed correlation).
     * Example: If 30, suggest 3; if 3, suggest 30
     */
    PAIR,

    /**
     * Delayed pair rule.
     * If number X appeared 2 spins ago, suggest number Y (pattern with 1-spin delay).
     * Example: 30 → [any number] → suggest 3
     * Reality: Patterns often don't trigger immediately but skip one spin.
     */
    DELAYED_PAIR,

    /**
     * Group correlation rule.
     * When any number from a group appears, others in the group + neighbors likely to follow.
     * Example: If any of [31, 33, 13, 11] appears → suggest the others + neighbors
     * Captures interconnected number clusters with high correlation.
     */
    GROUP_CORRELATION,

    /**
     * Sequence pattern rule.
     * If last N spins match pattern, suggest specific numbers.
     * Example: If last 3 spins were [30, 3, 15], suggest [22, 17]
     */
    SEQUENCE,

    /**
     * Hot streak rule.
     * If number X appeared K times in last N spins, suggest X again.
     * Example: If 17 appeared 3 times in last 10 spins, suggest 17
     */
    HOT_STREAK,

    /**
     * Cold number rule.
     * If number X hasn't appeared in N spins, suggest it.
     * Example: If 0 hasn't appeared in 50 spins, suggest 0
     */
    COLD_NUMBER,

    /**
     * Time-based rule.
     * Suggest numbers based on session duration or spin count.
     * Example: After 50 spins, certain numbers become more likely
     */
    TIME_BASED,

    /**
     * Color alternation rule.
     * If N consecutive reds/blacks, suggest opposite color numbers.
     * Example: If 3 reds in a row, suggest black numbers
     */
    COLOR_ALTERNATION,

    /**
     * Sector bounce rule.
     * If number in sector A, next often in sector B.
     * Example: If 0-9 sector, suggest 10-19 sector
     */
    SECTOR_BOUNCE,

    /**
     * Dozen cycle rule.
     * First dozen (1-12), second (13-24), third (25-36) rotation patterns.
     * Example: If 1st dozen hit 3 times, suggest 2nd dozen
     */
    DOZEN_CYCLE,

    /**
     * Even/Odd pattern rule.
     * Track odd/even streaks and alternations.
     * Example: If 3 odds in a row, suggest even numbers
     */
    EVEN_ODD_PATTERN,

    /**
     * Repeating distance rule.
     * If distance between last 2 numbers is X, next might be X away.
     * Example: 10 → 20 (distance 10) → suggest 30, 0
     */
    REPEATING_DISTANCE,

    /**
     * Mirror numbers rule.
     * Suggest opposite side of the wheel.
     * Example: If left sector, suggest right sector
     */
    MIRROR_NUMBERS,

    /**
     * Gap pattern rule.
     * Number appeared, then gap of exactly N spins, then appears again.
     * Example: Number appeared, missing for 20 spins, suggest it
     */
    GAP_PATTERN,

    /**
     * Streak breaker rule.
     * If same number hit 2+ times recently, suggest opposite characteristics.
     * Example: If 17 hit twice, suggest low red numbers
     */
    STREAK_BREAKER,

    /**
     * Multi-trigger rule.
     * Multiple conditions must be met simultaneously.
     * Example: Last 2 spins both red AND both odd → suggest specific numbers
     */
    MULTI_TRIGGER,

    /**
     * Custom condition rule.
     * Complex rule defined by custom logic.
     */
    CUSTOM
}
