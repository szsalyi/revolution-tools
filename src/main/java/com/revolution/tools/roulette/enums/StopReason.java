package com.revolution.tools.roulette.enums;

/**
 * Reason why a roulette session was stopped.
 */
public enum StopReason {
    /**
     * User manually stopped the session
     */
    MANUAL_STOP,

    /**
     * Stop-loss threshold reached
     */
    STOP_LOSS_HIT,

    /**
     * Take-profit target reached
     */
    TAKE_PROFIT_REACHED,

    /**
     * Maximum number of spins reached
     */
    MAX_SPINS_REACHED,

    /**
     * Maximum session duration exceeded
     */
    MAX_DURATION_REACHED,

    /**
     * Tilt detected - session locked for protection
     */
    TILT_DETECTED,

    /**
     * Bankroll depleted
     */
    BANKROLL_DEPLETED,

    /**
     * System error or crash
     */
    SYSTEM_ERROR
}
