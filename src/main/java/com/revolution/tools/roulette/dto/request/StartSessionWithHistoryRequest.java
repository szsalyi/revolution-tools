package com.revolution.tools.roulette.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Request to start a session with historical spin data.
 * Production-ready: Paste historical numbers from casino UI.
 *
 * Example:
 * POST /api/roulette/sessions/start-with-history
 * {
 *   "initialBankroll": 500.00,
 *   "stopLossPercent": -50,
 *   "takeProfitLevels": [20, 50, 100],
 *   "flatBetPercent": 30,
 *   "historicalSpins": [4, 17, 22, 2, 19, 34, 14, 7, 29, ...],  // Last 20-50 spins
 *   "maxSpins": 150,
 *   "maxDurationMinutes": 120
 * }
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StartSessionWithHistoryRequest extends StartSessionRequest {

    /**
     * Historical spin numbers to import (0-36).
     * Paste from casino UI - last 20-50 spins recommended.
     */
    @NotNull(message = "Historical spins are required")
    @Size(min = 10, max = 100, message = "Provide 10-100 historical spins for analysis")
    private List<Integer> historicalSpins;

    /**
     * Whether to analyze and return initial bet suggestions.
     * Default: true
     */
    private Boolean returnInitialSuggestions = true;
}
