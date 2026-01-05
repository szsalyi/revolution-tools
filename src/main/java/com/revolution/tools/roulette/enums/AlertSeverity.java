package com.revolution.tools.roulette.enums;

/**
 * Severity level of alerts.
 */
public enum AlertSeverity {
    /**
     * Informational message (e.g., new pattern detected)
     */
    INFO,

    /**
     * Warning message (e.g., giving back profit)
     */
    WARNING,

    /**
     * Critical alert (e.g., stop-loss hit, tilt detected)
     */
    CRITICAL
}
