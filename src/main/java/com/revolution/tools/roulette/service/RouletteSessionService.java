package com.revolution.tools.roulette.service;

import com.revolution.tools.roulette.dto.request.PlaceBetRequest;
import com.revolution.tools.roulette.dto.request.RecordSpinRequest;
import com.revolution.tools.roulette.dto.request.StartSessionRequest;
import com.revolution.tools.roulette.dto.request.StartSessionWithHistoryRequest;
import com.revolution.tools.roulette.dto.response.BetValidationResponse;
import com.revolution.tools.roulette.dto.response.SessionResponse;
import com.revolution.tools.roulette.dto.response.SpinResponse;
import com.revolution.tools.roulette.dto.response.StartSessionWithHistoryResponse;

import java.util.List;

/**
 * Core service for managing roulette gambling sessions.
 * Handles session lifecycle, spin recording, bet validation, and bankroll management.
 */
public interface RouletteSessionService {

    /**
     * Start a new roulette session with discipline parameters.
     *
     * @param request Session configuration (bankroll, stop-loss, take-profit, etc.)
     * @return SessionResponse with session ID and initial state
     */
    SessionResponse startSession(StartSessionRequest request);

    /**
     * Start a new session with historical spin data.
     * Production-ready: Paste historical numbers from casino UI.
     *
     * @param request Session configuration + historical spins
     * @return SessionResponse with session ID, imported spins count, and initial bet suggestions
     */
    StartSessionWithHistoryResponse startSessionWithHistory(StartSessionWithHistoryRequest request);

    /**
     * Record a spin result for a session.
     * Updates session statistics and checks for patterns.
     *
     * @param sessionId Session identifier
     * @param request Spin details
     * @return SpinResponse with updated session state
     */
    SpinResponse recordSpin(String sessionId, RecordSpinRequest request);

    /**
     * Validate a bet against patterns and discipline rules.
     * Does NOT place the bet - only validates it.
     *
     * @param sessionId Session identifier
     * @param request Bet details
     * @return BetValidationResponse with warnings/violations
     */
    BetValidationResponse validateBet(String sessionId, PlaceBetRequest request);

    /**
     * Place a bet (after validation).
     * Records the bet and updates bankroll.
     *
     * @param sessionId Session identifier
     * @param request Bet details
     * @return BetValidationResponse with bet ID
     */
    BetValidationResponse placeBet(String sessionId, PlaceBetRequest request);

    /**
     * Get current session state.
     *
     * @param sessionId Session identifier
     * @return SessionResponse with current state
     */
    SessionResponse getSession(String sessionId);

    /**
     * Stop a session manually.
     *
     * @param sessionId Session identifier
     * @return SessionResponse with final state
     */
    SessionResponse stopSession(String sessionId);

    /**
     * Get all active sessions.
     *
     * @return List of active sessions
     */
    List<SessionResponse> getActiveSessions();

    /**
     * Get spin history for a session.
     *
     * @param sessionId Session identifier
     * @param limit Number of spins to retrieve (default 50)
     * @return List of recent spins
     */
    List<SpinResponse> getSpinHistory(String sessionId, Integer limit);
}
