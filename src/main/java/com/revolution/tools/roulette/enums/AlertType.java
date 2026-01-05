package com.revolution.tools.roulette.enums;

/**
 * Type of alert/notification.
 */
public enum AlertType {
    // Discipline Alerts
    STOP_LOSS_HIT,
    TAKE_PROFIT_REACHED,
    MAX_SPINS_REACHED,
    MAX_DURATION_REACHED,
    TILT_DETECTED,
    STAKE_VIOLATION,

    // Profit Protection
    PROFIT_GIVEBACK_WARNING,
    PEAK_PROFIT_ALERT,

    // Rule Violations
    BET_RULE_VIOLATION,
    BET_NOT_IN_PATTERN,
    BET_ON_EXCLUDED_NUMBERS,
    STAKE_TOO_HIGH,
    STAKE_TOO_LOW,

    // Pattern Alerts
    HOT_NUMBERS_DETECTED,
    SECTION_CLUSTERING_DETECTED,
    MISSING_NUMBERS_DETECTED,
    PATTERN_SUGGESTION_AVAILABLE,

    // Session
    SESSION_STARTED,
    SESSION_ENDED,
    COOLDOWN_ACTIVE,
    COOLDOWN_ENDED
}
