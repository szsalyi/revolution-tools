package com.revolution.tools.roulette.service.impl;

import com.revolution.tools.roulette.dto.response.SessionHealthCheck;
import com.revolution.tools.roulette.entity.RouletteBet;
import com.revolution.tools.roulette.entity.RouletteSession;
import com.revolution.tools.roulette.enums.SessionStatus;
import com.revolution.tools.roulette.repository.RouletteBetRepository;
import com.revolution.tools.roulette.repository.RouletteSessionRepository;
import com.revolution.tools.roulette.service.DisciplineEnforcerService;
import com.revolution.tools.roulette.service.SessionHealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of SessionHealthCheckService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionHealthCheckServiceImpl implements SessionHealthCheckService {

    private final RouletteSessionRepository sessionRepository;
    private final RouletteBetRepository betRepository;
    private final DisciplineEnforcerService disciplineEnforcer;

    @Override
    @Transactional(readOnly = true)
    public SessionHealthCheck checkSessionHealth(String sessionId) {
        RouletteSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        return checkSessionHealthById(session.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public SessionHealthCheck checkSessionHealthById(Long id) {
        log.debug("Checking health for session ID: {}", id);

        RouletteSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));

        List<String> warnings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        String status = "OK";
        boolean healthy = true;

        // Check if session is stopped
        if (session.getStatus() != SessionStatus.ACTIVE) {
            warnings.add("⚠️ Session is " + session.getStatus());
            healthy = false;
            status = "STOPPED";

            if (session.getStopReason() != null) {
                recommendations.add("Session stopped: " + session.getStopReason());
            }
        }

        // Calculate distance to stop-loss
        Double stopLossDistance = calculateStopLossDistance(session);
        if (stopLossDistance != null && stopLossDistance < 20.0) {
            warnings.add(String.format("⚠️ STOP-LOSS CLOSE: Only %.1f%% away from stop-loss", stopLossDistance));
            recommendations.add("🛑 Consider stopping session - close to stop-loss");
            healthy = false;
            status = "CRITICAL";
        } else if (stopLossDistance != null && stopLossDistance < 40.0) {
            warnings.add(String.format("⚠️ Approaching stop-loss: %.1f%% away", stopLossDistance));
            recommendations.add("📉 Reduce bet sizes - approaching stop-loss");
            if (status.equals("OK")) status = "WARNING";
        }

        // Calculate distance to take-profit
        Double takeProfitDistance = calculateTakeProfitDistance(session);
        if (takeProfitDistance != null && takeProfitDistance < 10.0) {
            recommendations.add(String.format("🎯 Close to take-profit! Only %.1f%% away", takeProfitDistance));
        }

        // Check for tilt
        boolean tiltDetected = disciplineEnforcer.detectTilt(session.getId());
        if (tiltDetected) {
            warnings.add("⚠️ TILT DETECTED: You're betting emotionally");
            recommendations.add("🛑 Take a break - emotional betting detected");
            healthy = false;
            if (!status.equals("CRITICAL")) status = "WARNING";
        }

        // Calculate recommended max stake
        BigDecimal recommendedMaxStake = disciplineEnforcer.calculateRecommendedStake(session);

        // Calculate current average stake
        BigDecimal currentAverageStake = calculateAverageStake(session.getId());

        // Check for overbetting
        if (currentAverageStake.compareTo(recommendedMaxStake) > 0) {
            double overBetPercent = currentAverageStake.subtract(recommendedMaxStake)
                    .divide(recommendedMaxStake, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            warnings.add(String.format("⚠️ OVERBETTING: Your average stake (€%.2f) exceeds recommended (€%.2f) by %.0f%%",
                    currentAverageStake, recommendedMaxStake, overBetPercent));
            recommendations.add(String.format("📉 Reduce stake to max €%.2f", recommendedMaxStake));
            healthy = false;
            if (status.equals("OK")) status = "WARNING";
        }

        // Check violations
        if (session.getRuleViolations() > 5) {
            warnings.add(String.format("⚠️ Multiple rule violations: %d total", session.getRuleViolations()));
            recommendations.add("📋 Review and follow discipline rules");
            healthy = false;
            if (status.equals("OK")) status = "WARNING";
        }

        // Check tilt events
        if (session.getTiltEvents() > 2) {
            warnings.add(String.format("⚠️ Multiple tilt events detected: %d", session.getTiltEvents()));
            recommendations.add("🧘 Take a longer break - multiple emotional betting patterns");
            healthy = false;
            if (status.equals("OK")) status = "WARNING";
        }

        // Check max spins
        Integer spinsRemaining = null;
        if (session.getMaxSpins() != null) {
            spinsRemaining = session.getMaxSpins() - session.getTotalSpins();
            if (spinsRemaining <= 10) {
                warnings.add(String.format("⚠️ Close to max spins: %d spins remaining", spinsRemaining));
                recommendations.add("⏱️ Session will auto-stop soon at max spins");
            }
        }

        // Add positive recommendations if healthy
        if (healthy && warnings.isEmpty()) {
            recommendations.add("✅ Session is healthy - continue playing within discipline rules");
        }

        log.info("Session {} health check: status={}, healthy={}, warnings={}, tilt={}",
                session.getSessionId(), status, healthy, warnings.size(), tiltDetected);

        return SessionHealthCheck.builder()
                .healthy(healthy)
                .status(status)
                .currentBankroll(session.getCurrentBankroll())
                .profitLoss(session.getCurrentProfit())
                .profitPercent(session.getProfitPercent() != null ? session.getProfitPercent().doubleValue() : 0.0)
                .stopLossDistance(stopLossDistance)
                .takeProfitDistance(takeProfitDistance)
                .recommendedMaxStake(recommendedMaxStake)
                .currentAverageStake(currentAverageStake)
                .tiltDetected(tiltDetected)
                .violations(session.getRuleViolations())
                .totalSpins(session.getTotalSpins())
                .maxSpins(session.getMaxSpins())
                .warnings(warnings)
                .recommendations(recommendations)
                .stopReason(session.getStopReason() != null ? session.getStopReason().name() : null)
                .build();
    }

    /**
     * Calculate distance to stop-loss (percentage points).
     */
    private Double calculateStopLossDistance(RouletteSession session) {
        if (session.getStopLossPercent() == null) {
            return null;
        }

        BigDecimal currentProfitPercent = session.getProfitPercent();
        if (currentProfitPercent == null) {
            return null;
        }

        // Distance = current profit % - stop-loss %
        // Example: -10% current, -50% stop-loss → distance = 40%
        double distance = currentProfitPercent.doubleValue() - session.getStopLossPercent();
        return Math.abs(distance);
    }

    /**
     * Calculate distance to nearest take-profit level (percentage points).
     */
    private Double calculateTakeProfitDistance(RouletteSession session) {
        List<Integer> takeProfitLevels = session.getTakeProfitLevelsAsList();
        if (takeProfitLevels == null || takeProfitLevels.isEmpty()) {
            return null;
        }

        BigDecimal currentProfitPercent = session.getProfitPercent();
        if (currentProfitPercent == null) {
            return null;
        }

        // Find nearest take-profit level above current profit
        Double nearestDistance = null;
        for (Integer level : takeProfitLevels) {
            if (level > currentProfitPercent.doubleValue()) {
                double distance = level - currentProfitPercent.doubleValue();
                if (nearestDistance == null || distance < nearestDistance) {
                    nearestDistance = distance;
                }
            }
        }

        return nearestDistance;
    }

    /**
     * Calculate average stake from recent bets.
     */
    private BigDecimal calculateAverageStake(Long sessionId) {
        // Get last 10 bets
        List<RouletteBet> recentBets = betRepository.findTop10BySessionIdOrderByTimestampDesc(sessionId);

        if (recentBets.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalStake = recentBets.stream()
                .map(RouletteBet::getTotalStake)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalStake.divide(BigDecimal.valueOf(recentBets.size()), 2, RoundingMode.HALF_UP);
    }
}
