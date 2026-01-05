package com.revolution.tools.roulette.service;

import com.revolution.tools.roulette.dto.response.SessionHealthCheck;

/**
 * Service for real-time session health monitoring.
 * Detects overbetting, tilt, and rule violations.
 */
public interface SessionHealthCheckService {

    /**
     * Perform comprehensive health check on a session.
     *
     * @param sessionId Session ID (UUID string)
     * @return SessionHealthCheck with warnings and recommendations
     */
    SessionHealthCheck checkSessionHealth(String sessionId);

    /**
     * Perform health check using database ID.
     *
     * @param id Session database ID
     * @return SessionHealthCheck with warnings and recommendations
     */
    SessionHealthCheck checkSessionHealthById(Long id);
}
