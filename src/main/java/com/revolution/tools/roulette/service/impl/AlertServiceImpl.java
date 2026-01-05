package com.revolution.tools.roulette.service.impl;

import com.revolution.tools.roulette.entity.RouletteAlert;
import com.revolution.tools.roulette.entity.RouletteSession;
import com.revolution.tools.roulette.enums.AlertSeverity;
import com.revolution.tools.roulette.enums.AlertType;
import com.revolution.tools.roulette.repository.RouletteAlertRepository;
import com.revolution.tools.roulette.repository.RouletteSessionRepository;
import com.revolution.tools.roulette.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of AlertService for managing discipline alerts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final RouletteAlertRepository alertRepository;
    private final RouletteSessionRepository sessionRepository;

    @Override
    @Transactional
    public RouletteAlert createAlert(Long sessionId, AlertType alertType,
                                     AlertSeverity severity, String message) {
        log.info("Creating alert for session {}: type={}, severity={}, message={}",
                sessionId, alertType, severity, message);

        RouletteSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        RouletteAlert alert = RouletteAlert.builder()
                .sessionId(sessionId)
                .alertType(alertType)
                .severity(severity)
                .message(message)
                .timestamp(LocalDateTime.now())
                .acknowledged(false)
                .build();

        RouletteAlert savedAlert = alertRepository.save(alert);

        // Log critical alerts
        if (severity == AlertSeverity.CRITICAL) {
            log.warn("CRITICAL ALERT for session {}: {}", sessionId, message);
        }

        return savedAlert;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouletteAlert> getSessionAlerts(Long sessionId) {
        return alertRepository.findBySessionIdOrderByTimestampDesc(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouletteAlert> getUnacknowledgedAlerts(Long sessionId) {
        return alertRepository.findBySessionIdAndAcknowledgedFalseOrderByTimestampDesc(sessionId);
    }

    @Override
    @Transactional
    public void acknowledgeAlert(Long alertId) {
        RouletteAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        alert.setAcknowledged(true);
        alertRepository.save(alert);

        log.debug("Alert {} acknowledged", alertId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCriticalAlerts(Long sessionId) {
        List<RouletteAlert> criticalAlerts = alertRepository
                .findBySessionIdAndSeverityAndAcknowledgedFalse(sessionId, AlertSeverity.CRITICAL);

        return !criticalAlerts.isEmpty();
    }
}
