package com.revolution.tools.roulette.enums;

/**
 * Type of pattern detected in spin history.
 */
public enum PatternType {
    /**
     * Numbers appearing frequently in recent history
     */
    HOT_NUMBERS,

    /**
     * Numbers adjacent on the wheel
     */
    NEIGHBORS,

    /**
     * Numbers that haven't appeared in many spins (for exclusion)
     */
    MISSING_NUMBERS,

    /**
     * Numbers clustered in 1-2 sections of the wheel
     */
    SECTION_CLUSTERING,

    /**
     * Double-digit pattern (e.g., 29 → 11)
     */
    DOUBLE_DIGIT,

    /**
     * Same number repeated in consecutive spins
     */
    REPEATING_NUMBER,

    /**
     * Numbers from specific dozen (1st, 2nd, 3rd)
     */
    DOZEN_PATTERN,

    /**
     * Numbers from specific column
     */
    COLUMN_PATTERN
}
