package com.revolution.tools.roulette.controller;

import com.revolution.tools.roulette.dto.request.PlaceBetRequest;
import com.revolution.tools.roulette.dto.request.QuickSpinRequest;
import com.revolution.tools.roulette.dto.request.RecordSpinRequest;
import com.revolution.tools.roulette.dto.request.StartSessionRequest;
import com.revolution.tools.roulette.dto.request.StartSessionWithHistoryRequest;
import com.revolution.tools.roulette.dto.response.BetSlipAnalysisResponse;
import com.revolution.tools.roulette.dto.response.BetValidationResponse;
import com.revolution.tools.roulette.dto.response.PatternSuggestionResponse;
import com.revolution.tools.roulette.dto.response.QuickSpinWithSuggestionsResponse;
import com.revolution.tools.roulette.dto.response.SessionHealthCheck;
import com.revolution.tools.roulette.dto.response.SessionResponse;
import com.revolution.tools.roulette.dto.response.SmartBetSuggestion;
import com.revolution.tools.roulette.dto.response.SpinResponse;
import com.revolution.tools.roulette.dto.response.StartSessionWithHistoryResponse;
import com.revolution.tools.roulette.service.BetSlipAnalyzerService;
import com.revolution.tools.roulette.service.PatternAnalyzerService;
import com.revolution.tools.roulette.service.RouletteSessionService;
import com.revolution.tools.roulette.service.SessionHealthCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for roulette discipline assistant.
 * Provides endpoints for session management, spin recording, bet validation, and pattern analysis.
 */
@Slf4j
@RestController
@RequestMapping("/api/roulette")
@RequiredArgsConstructor
public class RouletteController {

    private final RouletteSessionService sessionService;
    private final PatternAnalyzerService patternAnalyzer;
    private final com.revolution.tools.roulette.service.SmartBetSuggestionService smartBetSuggestionService;
    private final SessionHealthCheckService sessionHealthCheckService;
    private final BetSlipAnalyzerService betSlipAnalyzerService;

    /**
     * Start a new roulette session.
     *
     * POST /api/roulette/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> startSession(@Valid @RequestBody StartSessionRequest request) {
        log.info("Starting new session with bankroll: {}", request.getInitialBankroll());
        SessionResponse response = sessionService.startSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Start a session with historical spin data.
     * Production-ready: Paste historical numbers from casino UI.
     *
     * POST /api/roulette/sessions/start-with-history
     * {
     *   "initialBankroll": 500.00,
     *   "historicalSpins": [4, 17, 22, 2, 19, 34, ...],
     *   ...
     * }
     */
    @PostMapping("/sessions/start-with-history")
    public ResponseEntity<StartSessionWithHistoryResponse> startSessionWithHistory(
            @Valid @RequestBody StartSessionWithHistoryRequest request) {
        log.info("Starting session with {} historical spins", request.getHistoricalSpins().size());
        StartSessionWithHistoryResponse response = sessionService.startSessionWithHistory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get session details.
     *
     * GET /api/roulette/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable String sessionId) {
        log.debug("Fetching session: {}", sessionId);
        SessionResponse response = sessionService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active sessions.
     *
     * GET /api/roulette/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getActiveSessions() {
        log.debug("Fetching all active sessions");
        List<SessionResponse> sessions = sessionService.getActiveSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * Stop a session manually.
     *
     * POST /api/roulette/sessions/{sessionId}/stop
     */
    @PostMapping("/sessions/{sessionId}/stop")
    public ResponseEntity<SessionResponse> stopSession(@PathVariable String sessionId) {
        log.info("Stopping session: {}", sessionId);
        SessionResponse response = sessionService.stopSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Record a spin result.
     *
     * POST /api/roulette/sessions/{sessionId}/spins
     */
    @PostMapping("/sessions/{sessionId}/spins")
    public ResponseEntity<SpinResponse> recordSpin(
            @PathVariable String sessionId,
            @Valid @RequestBody RecordSpinRequest request) {
        log.info("Recording spin {} for session {}", request.getSpinNumber(), sessionId);
        SpinResponse response = sessionService.recordSpin(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get spin history for a session.
     *
     * GET /api/roulette/sessions/{sessionId}/spins?limit=50
     */
    @GetMapping("/sessions/{sessionId}/spins")
    public ResponseEntity<List<SpinResponse>> getSpinHistory(
            @PathVariable String sessionId,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {
        log.debug("Fetching spin history for session {} (limit: {})", sessionId, limit);
        List<SpinResponse> spins = sessionService.getSpinHistory(sessionId, limit);
        return ResponseEntity.ok(spins);
    }

    /**
     * Validate a bet before placing it.
     *
     * POST /api/roulette/sessions/{sessionId}/bets/validate
     */
    @PostMapping("/sessions/{sessionId}/bets/validate")
    public ResponseEntity<BetValidationResponse> validateBet(
            @PathVariable String sessionId,
            @Valid @RequestBody PlaceBetRequest request) {
        log.info("Validating bet for session {}: numbers={}", sessionId, request.getNumbers());
        BetValidationResponse response = sessionService.validateBet(sessionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Place a bet (records the bet).
     *
     * POST /api/roulette/sessions/{sessionId}/bets
     */
    @PostMapping("/sessions/{sessionId}/bets")
    public ResponseEntity<BetValidationResponse> placeBet(
            @PathVariable String sessionId,
            @Valid @RequestBody PlaceBetRequest request) {
        log.info("Placing bet for session {}: {} bet items, totalStake={}",
                sessionId, request.getBetItems().size(), request.getTotalStake());
        BetValidationResponse response = sessionService.placeBet(sessionId, request);

        HttpStatus status = response.getValid() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Analyze bet slip screenshot using Claude AI Vision.
     * Production-ready: Upload screenshot, AI extracts bet details automatically!
     *
     * POST /api/roulette/sessions/{sessionId}/bets/from-screenshot
     *
     * @param sessionId Session ID
     * @param screenshot Bet slip image (PNG, JPG, etc.)
     * @param autoRecord Whether to automatically record the bet (default: false)
     * @return Analysis response with extracted bet items and optional bet ID
     */
    @PostMapping("/sessions/{sessionId}/bets/from-screenshot")
    public ResponseEntity<BetSlipAnalysisResponse> analyzeBetSlipScreenshot(
            @PathVariable String sessionId,
            @RequestParam("screenshot") MultipartFile screenshot,
            @RequestParam(value = "autoRecord", defaultValue = "false") Boolean autoRecord) {
        log.info("Analyzing bet slip screenshot for session {}: file={}, size={}, autoRecord={}",
                sessionId, screenshot.getOriginalFilename(), screenshot.getSize(), autoRecord);

        BetSlipAnalysisResponse response;

        if (autoRecord) {
            // Analyze and automatically record the bet
            response = betSlipAnalyzerService.analyzeAndRecordBet(sessionId, screenshot);
        } else {
            // Just analyze without recording
            response = betSlipAnalyzerService.analyzeScreenshot(screenshot);
        }

        HttpStatus status = response.getBetDetected() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Get pattern suggestions for a session.
     *
     * GET /api/roulette/sessions/{sessionId}/patterns
     */
    @GetMapping("/sessions/{sessionId}/patterns")
    public ResponseEntity<PatternSuggestionResponse> getPatternSuggestions(
            @PathVariable String sessionId) {
        log.info("Analyzing patterns for session {}", sessionId);

        // Get session to extract ID
        SessionResponse session = sessionService.getSession(sessionId);

        PatternSuggestionResponse response = patternAnalyzer.analyzePatterns(session.getId());
        return ResponseEntity.ok(response);
    }

    // ========== PRODUCTION-READY ENDPOINTS ==========

    /**
     * Quick spin recording with auto-suggestions.
     * Production-ready: Record spin + get next bet suggestions automatically!
     *
     * POST /api/roulette/sessions/{sessionId}/spins/quick
     * { "number": 17 }
     *
     * Returns: Spin confirmation + next bet suggestions + session health
     */
    @PostMapping("/sessions/{sessionId}/spins/quick")
    public ResponseEntity<QuickSpinWithSuggestionsResponse> quickSpin(
            @PathVariable String sessionId,
            @Valid @RequestBody QuickSpinRequest request) {
        log.info("Quick spin: session={}, number={}", sessionId, request.getNumber());

        // Convert to RecordSpinRequest
        RecordSpinRequest recordRequest = RecordSpinRequest.builder()
                .spinNumber(request.getNumber())
                .build();

        // Record spin
        SpinResponse spinResponse = sessionService.recordSpin(sessionId, recordRequest);

        // Get session
        SessionResponse session = sessionService.getSession(sessionId);

        // Generate next bet suggestions automatically
        SmartBetSuggestion nextSuggestions = smartBetSuggestionService.generateSuggestions(session.getId());

        // Perform comprehensive health check
        SessionHealthCheck health = sessionHealthCheckService.checkSessionHealthById(session.getId());

        QuickSpinWithSuggestionsResponse response = QuickSpinWithSuggestionsResponse.builder()
                .spinRecorded(spinResponse)
                .nextSuggestions(nextSuggestions)
                .sessionHealth(health)
                .sessionStatus(session)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get smart bet suggestions with ready-to-use weighted BetItems.
     * Production-ready: Returns exactly what you need to place a bet.
     *
     * GET /api/roulette/sessions/{sessionId}/suggestions/smart
     */
    @GetMapping("/sessions/{sessionId}/suggestions/smart")
    public ResponseEntity<SmartBetSuggestion> getSmartSuggestions(
            @PathVariable String sessionId) {
        log.info("Getting smart bet suggestions for session: {}", sessionId);

        SessionResponse session = sessionService.getSession(sessionId);
        SmartBetSuggestion suggestion = smartBetSuggestionService.generateSuggestions(session.getId());

        return ResponseEntity.ok(suggestion);
    }

    /**
     * Real-time session health check.
     * Production-ready: Warns about overbetting, violations, and tilt.
     *
     * GET /api/roulette/sessions/{sessionId}/health
     */
    @GetMapping("/sessions/{sessionId}/health")
    public ResponseEntity<SessionHealthCheck> getSessionHealth(
            @PathVariable String sessionId) {
        log.info("Checking health for session: {}", sessionId);

        SessionHealthCheck health = sessionHealthCheckService.checkSessionHealth(sessionId);
        return ResponseEntity.ok(health);
    }

    /**
     * Health check endpoint.
     *
     * GET /api/roulette/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Roulette Discipline Assistant is running");
    }
}
