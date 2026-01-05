package com.revolution.tools.roulette.service.impl;

import com.revolution.tools.roulette.dto.request.PlaceBetRequest;
import com.revolution.tools.roulette.dto.request.RecordSpinRequest;
import com.revolution.tools.roulette.dto.request.StartSessionRequest;
import com.revolution.tools.roulette.dto.request.StartSessionWithHistoryRequest;
import com.revolution.tools.roulette.dto.response.BetValidationResponse;
import com.revolution.tools.roulette.dto.response.SessionResponse;
import com.revolution.tools.roulette.dto.response.SmartBetSuggestion;
import com.revolution.tools.roulette.dto.response.SpinResponse;
import com.revolution.tools.roulette.dto.response.StartSessionWithHistoryResponse;
import com.revolution.tools.roulette.entity.RouletteBet;
import com.revolution.tools.roulette.entity.RouletteSession;
import com.revolution.tools.roulette.entity.RouletteSpin;
import com.revolution.tools.roulette.enums.AlertSeverity;
import com.revolution.tools.roulette.enums.AlertType;
import com.revolution.tools.roulette.enums.BetType;
import com.revolution.tools.roulette.enums.SessionStatus;
import com.revolution.tools.roulette.enums.StopReason;
import com.revolution.tools.roulette.repository.RouletteBetRepository;
import com.revolution.tools.roulette.repository.RouletteSessionRepository;
import com.revolution.tools.roulette.repository.RouletteSpinRepository;
import com.revolution.tools.roulette.service.AlertService;
import com.revolution.tools.roulette.service.DisciplineEnforcerService;
import com.revolution.tools.roulette.service.PatternAnalyzerService;
import com.revolution.tools.roulette.service.RouletteSessionService;
import com.revolution.tools.roulette.service.SmartBetSuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of RouletteSessionService for managing gambling sessions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouletteSessionServiceImpl implements RouletteSessionService {

    private final RouletteSessionRepository sessionRepository;
    private final RouletteSpinRepository spinRepository;
    private final RouletteBetRepository betRepository;
    private final AlertService alertService;
    private final DisciplineEnforcerService disciplineEnforcer;
    private final PatternAnalyzerService patternAnalyzer;
    private final SmartBetSuggestionService smartBetSuggestionService;

    @Override
    @Transactional
    public SessionResponse startSession(StartSessionRequest request) {
        log.info("Starting new roulette session: initialBankroll={}, stopLoss={}%, takeProfit={}",
                request.getInitialBankroll(), request.getStopLossPercent(), request.getTakeProfitLevels());

        String sessionId = UUID.randomUUID().toString();

        RouletteSession session = RouletteSession.builder()
                .sessionId(sessionId)
                .initialBankroll(request.getInitialBankroll())
                .currentBankroll(request.getInitialBankroll())
                .peakProfit(BigDecimal.ZERO)
                .currentProfit(BigDecimal.ZERO)
                .stopLossPercent(request.getStopLossPercent())
                .flatBetMinPercent(request.getFlatBetPercent())
                .flatBetMaxPercent(request.getFlatBetPercent() + 10) // Add 10% tolerance
                .maxSpins(request.getMaxSpins())
                .maxDurationMinutes(request.getMaxDurationMinutes())
                .status(SessionStatus.ACTIVE)
                .startTime(LocalDateTime.now())
                .totalSpins(0)
                .totalBets(0)
                .ruleViolations(0)
                .tiltEvents(0)
                .build();

        // Set take profit levels from list
        session.setTakeProfitLevelsFromList(request.getTakeProfitLevels());

        RouletteSession savedSession = sessionRepository.save(session);

        log.info("Session {} started successfully", sessionId);

        return mapToSessionResponse(savedSession);
    }

    @Override
    @Transactional
    public StartSessionWithHistoryResponse startSessionWithHistory(StartSessionWithHistoryRequest request) {
        log.info("Starting session with {} historical spins", request.getHistoricalSpins().size());

        // Start normal session first
        SessionResponse session = startSession(request);

        // Import historical spins
        int spinCount = 0;
        for (Integer spinNumber : request.getHistoricalSpins()) {
            RecordSpinRequest spinRequest = RecordSpinRequest.builder()
                    .spinNumber(spinNumber)
                    .build();

            recordSpin(session.getSessionId(), spinRequest);
            spinCount++;
        }

        log.info("Imported {} historical spins for session {}", spinCount, session.getSessionId());

        // Generate initial bet suggestions if requested
        SmartBetSuggestion initialSuggestions = null;
        if (request.getReturnInitialSuggestions() != null && request.getReturnInitialSuggestions()) {
            // Get session by sessionId to find the ID
            RouletteSession rouletteSession = findSessionBySessionId(session.getSessionId());
            initialSuggestions = smartBetSuggestionService.generateSuggestions(rouletteSession.getId());
            log.info("Generated initial suggestions: {} bet items with {} confidence",
                    initialSuggestions.getBetItems().size(), initialSuggestions.getConfidence());
        }

        return StartSessionWithHistoryResponse.builder()
                .session(session)
                .spinsImported(spinCount)
                .initialSuggestions(initialSuggestions)
                .detectedPatterns(initialSuggestions != null ? initialSuggestions.getHotSections() : null)
                .build();
    }

    @Override
    @Transactional
    public SpinResponse recordSpin(String sessionId, RecordSpinRequest request) {
        log.debug("Recording spin for session {}: spinNumber={}", sessionId, request.getSpinNumber());

        RouletteSession session = findSessionBySessionId(sessionId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active: " + session.getStatus());
        }

        // Create spin record
        RouletteSpin spin = RouletteSpin.builder()
                .sessionId(session.getId())
                .spinNumber(request.getSpinNumber())
                .sequenceNumber(session.getTotalSpins() + 1)
                .timestamp(LocalDateTime.now())
                .build();

        RouletteSpin savedSpin = spinRepository.save(spin);

        // Update session spin count
        session.setTotalSpins(session.getTotalSpins() + 1);

        // Check if any pending bets won/lost
        updatePendingBets(session, request.getSpinNumber());

        // Check discipline rules
        checkDisciplineRules(session);

        sessionRepository.save(session);

        return mapToSpinResponse(savedSpin);
    }

    @Override
    @Transactional(readOnly = true)
    public BetValidationResponse validateBet(String sessionId, PlaceBetRequest request) {
        log.debug("Validating bet for session {}: {} bet items, totalStake={}",
                sessionId, request.getBetItems().size(), request.getTotalStake());

        RouletteSession session = findSessionBySessionId(sessionId);

        List<String> warnings = new ArrayList<>();
        List<String> violations = new ArrayList<>();

        // Get bet items (handles both old and new format)
        List<com.revolution.tools.roulette.dto.BetItem> betItems = request.getBetItems();

        if (betItems.isEmpty()) {
            violations.add("No bet items provided");
            return BetValidationResponse.builder()
                    .valid(false)
                    .violations(violations)
                    .build();
        }

        // Validate total stake and calculate average for discipline check
        BigDecimal totalStake = request.getTotalStake();
        BigDecimal avgStake = totalStake.divide(BigDecimal.valueOf(betItems.size()), 2, java.math.RoundingMode.HALF_UP);

        List<String> stakeViolations = disciplineEnforcer.validateStake(
                session, avgStake, betItems.size());
        violations.addAll(stakeViolations);

        // Check pattern matching
        List<Integer> numbers = betItems.stream()
                .map(com.revolution.tools.roulette.dto.BetItem::getNumber)
                .collect(Collectors.toList());

        boolean matchesPattern = patternAnalyzer.betMatchesPatterns(session.getId(), numbers);

        if (!matchesPattern) {
            warnings.add("Bet does not match detected patterns - consider reviewing pattern suggestions");
        }

        // Check for tilt
        boolean wouldCauseTilt = disciplineEnforcer.detectTilt(session.getId());
        if (wouldCauseTilt) {
            violations.add("TILT DETECTED - Take a break and reassess your strategy");
        }

        // Get recommended stake
        BigDecimal recommendedStake = disciplineEnforcer.calculateRecommendedStake(session);

        boolean valid = violations.isEmpty();

        return BetValidationResponse.builder()
                .valid(valid)
                .warnings(warnings)
                .violations(violations)
                .matchesPattern(matchesPattern)
                .wouldCauseTilt(wouldCauseTilt)
                .recommendedStake(recommendedStake)
                .build();
    }

    @Override
    @Transactional
    public BetValidationResponse placeBet(String sessionId, PlaceBetRequest request) {
        log.info("Placing bet for session {}: {} bet items, totalStake={}",
                sessionId, request.getBetItems().size(), request.getTotalStake());

        RouletteSession session = findSessionBySessionId(sessionId);

        // Validate bet first
        BetValidationResponse validation = validateBet(sessionId, request);

        // Create bet record using BetItem structure
        RouletteBet bet = createBetFromRequest(session, request, validation);

        RouletteBet savedBet = betRepository.save(bet);

        // Update session
        session.setTotalBets(session.getTotalBets() + 1);
        if (!validation.getValid()) {
            session.setRuleViolations(session.getRuleViolations() + 1);

            // Create alert for violation
            alertService.createAlert(session.getId(), AlertType.BET_RULE_VIOLATION,
                    AlertSeverity.WARNING,
                    "Bet placed despite violations: " + String.join("; ", validation.getViolations()));
        }

        if (validation.getWouldCauseTilt()) {
            session.setTiltEvents(session.getTiltEvents() + 1);

            // Create critical alert for tilt
            alertService.createAlert(session.getId(), AlertType.TILT_DETECTED,
                    AlertSeverity.CRITICAL,
                    "TILT DETECTED - Emotional betting pattern identified");
        }

        sessionRepository.save(session);

        validation.setBetId(savedBet.getId());
        return validation;
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getSession(String sessionId) {
        RouletteSession session = findSessionBySessionId(sessionId);
        return mapToSessionResponse(session);
    }

    @Override
    @Transactional
    public SessionResponse stopSession(String sessionId) {
        log.info("Stopping session {}", sessionId);

        RouletteSession session = findSessionBySessionId(sessionId);

        session.setStatus(SessionStatus.STOPPED);
        session.setStopReason(StopReason.MANUAL_STOP);
        session.setEndedAt(LocalDateTime.now());

        RouletteSession stoppedSession = sessionRepository.save(session);

        return mapToSessionResponse(stoppedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessions() {
        return sessionRepository.findByStatus(SessionStatus.ACTIVE).stream()
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpinResponse> getSpinHistory(String sessionId, Integer limit) {
        RouletteSession session = findSessionBySessionId(sessionId);
        int spinLimit = limit != null ? limit : 50;

        return spinRepository.findLastNSpins(session.getId(), spinLimit).stream()
                .map(this::mapToSpinResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private RouletteSession findSessionBySessionId(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    /**
     * Create RouletteBet from PlaceBetRequest (supports both old and new formats).
     */
    private RouletteBet createBetFromRequest(RouletteSession session,
                                              PlaceBetRequest request,
                                              BetValidationResponse validation) {
        List<com.revolution.tools.roulette.dto.BetItem> betItems = request.getBetItems();

        if (betItems.isEmpty()) {
            throw new IllegalArgumentException("No bet items found in request");
        }

        // Build weighted bet data
        Map<Integer, RouletteBet.BetData> betDataMap = new LinkedHashMap<>();
        for (com.revolution.tools.roulette.dto.BetItem item : betItems) {
            betDataMap.put(item.getNumber(),
                    new RouletteBet.BetData(item.getSource(), item.getStake()));
        }

        RouletteBet bet = new RouletteBet();
        bet.setSessionId(session.getId());
        bet.setWeightedBetData(betDataMap);
        bet.setTotalStake(request.getTotalStake());
        bet.setValidated(validation.getValid());
        bet.setValidationResult(validation.getValid() ? "VALID" : String.join("; ", validation.getViolations()));
        bet.setTimestamp(LocalDateTime.now());

        return bet;
    }

    private void updatePendingBets(RouletteSession session, Integer spinNumber) {
        List<RouletteBet> pendingBets = betRepository.findBySessionIdAndResultSpinNumberIsNull(session.getId());

        for (RouletteBet bet : pendingBets) {
            bet.calculateResult(spinNumber);
            betRepository.save(bet);

            // Update session bankroll
            session.updateBankroll(bet.getWinLoss());
        }
    }

    private void checkDisciplineRules(RouletteSession session) {
        // Check stop-loss
        if (disciplineEnforcer.isStopLossHit(session)) {
            session.setStatus(SessionStatus.STOPPED);
            session.setStopReason(StopReason.STOP_LOSS_HIT);
            session.setEndedAt(LocalDateTime.now());

            alertService.createAlert(session.getId(), AlertType.STOP_LOSS_HIT,
                    AlertSeverity.CRITICAL,
                    "STOP-LOSS HIT: Session stopped at " + session.getCurrentProfit());

            log.warn("Session {} stopped: STOP-LOSS HIT", session.getSessionId());
        }

        // Check take-profit
        if (disciplineEnforcer.isTakeProfitReached(session)) {
            session.setStatus(SessionStatus.STOPPED);
            session.setStopReason(StopReason.TAKE_PROFIT_REACHED);
            session.setEndedAt(LocalDateTime.now());

            alertService.createAlert(session.getId(), AlertType.TAKE_PROFIT_REACHED,
                    AlertSeverity.INFO,
                    "TAKE-PROFIT REACHED: Session stopped at " + session.getCurrentBankroll());

            log.info("Session {} stopped: TAKE-PROFIT REACHED", session.getSessionId());
        }

        // Check max spins
        if (disciplineEnforcer.isMaxSpinsReached(session)) {
            session.setStatus(SessionStatus.STOPPED);
            session.setStopReason(StopReason.MAX_SPINS_REACHED);
            session.setEndedAt(LocalDateTime.now());

            log.info("Session {} stopped: MAX SPINS REACHED", session.getSessionId());
        }
    }

    private SessionResponse mapToSessionResponse(RouletteSession session) {
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .initialBankroll(session.getInitialBankroll())
                .currentBankroll(session.getCurrentBankroll())
                .currentProfit(session.getCurrentProfit())
                .profitPercent(session.getProfitPercent())
                .status(session.getStatus())
                .stopReason(session.getStopReason())
                .totalSpins(session.getTotalSpins())
                .totalBets(session.getTotalBets())
                .ruleViolations(session.getRuleViolations())
                .tiltEvents(session.getTiltEvents())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .build();
    }

    private SpinResponse mapToSpinResponse(RouletteSpin spin) {
        RouletteSession session = sessionRepository.findById(spin.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        return SpinResponse.builder()
                .spinId(spin.getId())
                .sessionId(session.getSessionId())
                .spinNumber(spin.getSpinNumber())
                .color(spin.getColor())
                .section(spin.getSection())
                .sequenceNumber(spin.getSequenceNumber())
                .timestamp(spin.getTimestamp())
                .build();
    }
}
