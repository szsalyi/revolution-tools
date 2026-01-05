package com.revolution.tools.roulette.service.impl;

import com.revolution.tools.roulette.entity.RouletteBet;
import com.revolution.tools.roulette.entity.RouletteSession;
import com.revolution.tools.roulette.repository.RouletteBetRepository;
import com.revolution.tools.roulette.service.DisciplineEnforcerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DisciplineEnforcerService for enforcing gambling discipline.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisciplineEnforcerServiceImpl implements DisciplineEnforcerService {

    private final RouletteBetRepository betRepository;

    @Override
    public boolean shouldStopSession(RouletteSession session) {
        return isStopLossHit(session)
                || isTakeProfitReached(session)
                || isMaxSpinsReached(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> validateStake(RouletteSession session, BigDecimal stakePerNumber, int numberCount) {
        List<String> violations = new ArrayList<>();

        BigDecimal totalStake = stakePerNumber.multiply(BigDecimal.valueOf(numberCount));
        BigDecimal currentBankroll = session.getCurrentBankroll();

        // Check if total stake exceeds current bankroll
        if (totalStake.compareTo(currentBankroll) > 0) {
            violations.add("Total stake (" + totalStake + ") exceeds current bankroll (" + currentBankroll + ")");
        }

        // Calculate flat bet limits
        BigDecimal minFlatBet = currentBankroll
                .multiply(BigDecimal.valueOf(session.getFlatBetMinPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal maxFlatBet = currentBankroll
                .multiply(BigDecimal.valueOf(session.getFlatBetMaxPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Check if total stake is within flat betting range
        if (totalStake.compareTo(minFlatBet) < 0) {
            violations.add("Total stake (" + totalStake + ") is below minimum flat bet (" + minFlatBet + ")");
        }

        if (totalStake.compareTo(maxFlatBet) > 0) {
            violations.add("Total stake (" + totalStake + ") exceeds maximum flat bet (" + maxFlatBet + ")");
        }

        // Check for progressive betting (martingale detection)
        List<RouletteBet> recentBets = betRepository.findLastNBets(session.getId(), 5);
        if (!recentBets.isEmpty()) {
            BigDecimal lastTotalStake = recentBets.get(0).getTotalStake();
            BigDecimal stakeIncrease = totalStake.subtract(lastTotalStake)
                    .divide(lastTotalStake, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (stakeIncrease.compareTo(BigDecimal.valueOf(50)) > 0) {
                violations.add("Stake increase of " + stakeIncrease.setScale(1, RoundingMode.HALF_UP)
                        + "% detected - possible martingale/progressive betting");
            }
        }

        return violations;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean detectTilt(Long sessionId) {
        // Check for stake escalation in last 5 bets
        List<RouletteBet> last5Bets = betRepository.findLastNBets(sessionId, 5);

        if (last5Bets.size() >= 2) {
            BigDecimal firstStake = last5Bets.get(last5Bets.size() - 1).getTotalStake();
            BigDecimal lastStake = last5Bets.get(0).getTotalStake();

            BigDecimal stakeIncrease = lastStake.subtract(firstStake)
                    .divide(firstStake, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (stakeIncrease.compareTo(BigDecimal.valueOf(50)) > 0) {
                log.warn("Tilt detected: stake escalation of {}% in last 5 bets", stakeIncrease);
                return true;
            }
        }

        // Check for rule violations in last 10 bets
        List<RouletteBet> last10Bets = betRepository.findLastNBets(sessionId, 10);
        long violationCount = last10Bets.stream()
                .filter(bet -> bet.getValidated() != null && !bet.getValidated())
                .count();

        if (violationCount >= 3) {
            log.warn("Tilt detected: {} rule violations in last 10 bets", violationCount);
            return true;
        }

        return false;
    }

    @Override
    public BigDecimal calculateRecommendedStake(RouletteSession session) {
        BigDecimal currentBankroll = session.getCurrentBankroll();
        Integer flatBetPercent = session.getFlatBetMinPercent(); // Use minimum for safety

        return currentBankroll
                .multiply(BigDecimal.valueOf(flatBetPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean isStopLossHit(RouletteSession session) {
        BigDecimal currentProfit = session.getCurrentProfit();
        BigDecimal initialBankroll = session.getInitialBankroll();

        BigDecimal stopLossPercent = BigDecimal.valueOf(session.getStopLossPercent() != null ? session.getStopLossPercent() : -20);
        BigDecimal stopLossAmount = initialBankroll
                .multiply(stopLossPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        boolean hit = currentProfit.compareTo(stopLossAmount) <= 0;

        if (hit) {
            log.warn("Stop-loss HIT: currentProfit={}, stopLossAmount={}", currentProfit, stopLossAmount);
        }

        return hit;
    }

    @Override
    public boolean isTakeProfitReached(RouletteSession session) {
        BigDecimal currentBankroll = session.getCurrentBankroll();
        List<Integer> takeProfitLevels = session.getTakeProfitLevelsAsList();

        if (takeProfitLevels.isEmpty()) {
            return false;
        }

        for (Integer level : takeProfitLevels) {
            BigDecimal targetBankroll = session.getInitialBankroll()
                    .multiply(BigDecimal.valueOf(level))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (currentBankroll.compareTo(targetBankroll) >= 0) {
                log.info("Take-profit REACHED: currentBankroll={}, targetLevel={}%, targetBankroll={}",
                        currentBankroll, level, targetBankroll);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isMaxSpinsReached(RouletteSession session) {
        if (session.getMaxSpins() == null) {
            return false;
        }

        boolean reached = session.getTotalSpins() >= session.getMaxSpins();

        if (reached) {
            log.info("Max spins REACHED: totalSpins={}, maxSpins={}",
                    session.getTotalSpins(), session.getMaxSpins());
        }

        return reached;
    }
}
