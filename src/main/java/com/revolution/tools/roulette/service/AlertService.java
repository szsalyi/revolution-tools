package com.revolution.tools.roulette.service;

import com.revolution.tools.roulette.entity.RouletteAlert;
import com.revolution.tools.roulette.enums.AlertSeverity;
import com.revolution.tools.roulette.enums.AlertType;

import java.util.List;

/**
 * Service for managing discipline alerts and alarms.
 * Sends notifications when rules are violated.
 */
public interface AlertService {

    /**
     * Create and send an alert for a session.
     *
     * @param sessionId Session identifier
     * @param alertType Type of alert
     * @param severity Alert severity
     * @param message Alert message
     * @return Created alert
     */
    RouletteAlert createAlert(Long sessionId, AlertType alertType,
                             AlertSeverity severity, String message);

    /**
     * Get all alerts for a session.
     *
     * @param sessionId Session identifier
     * @return List of alerts
     */
    List<RouletteAlert> getSessionAlerts(Long sessionId);

    /**
     * Get unacknowledged alerts for a session.
     *
     * @param sessionId Session identifier
     * @return List of unacknowledged alerts
     */
    List<RouletteAlert> getUnacknowledgedAlerts(Long sessionId);

    /**
     * Acknowledge an alert.
     *
     * @param alertId Alert identifier
     */
    void acknowledgeAlert(Long alertId);

    /**
     * Check if session has critical unacknowledged alerts.
     *
     * @param sessionId Session identifier
     * @return true if critical alerts exist
     */
    boolean hasCriticalAlerts(Long sessionId);
}
