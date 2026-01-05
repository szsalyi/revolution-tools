package com.revolution.tools.roulette.enums;

/**
 * Status of a roulette playing session.
 */
public enum SessionStatus {
    /**
     * Session is active and accepting spins/bets
     */
    ACTIVE,

    /**
     * Session manually stopped by user
     */
    STOPPED,

    /**
     * Session completed normally (reached end condition)
     */
    COMPLETED,

    /**
     * Session locked due to discipline violation (e.g., tilt detected)
     */
    LOCKED,

    /**
     * Session in cooldown period (cannot start new session yet)
     */
    COOLDOWN
}
