package com.revolution.tools.roulette.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request to start a new roulette session.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StartSessionRequest {

    @NotNull(message = "Initial bankroll is required")
    @DecimalMin(value = "1.00", message = "Initial bankroll must be at least 1.00")
    @DecimalMax(value = "100000.00", message = "Initial bankroll cannot exceed 100,000")
    private BigDecimal initialBankroll;

    @Min(value = -100, message = "Stop loss percent must be between -100 and 0")
    @Max(value = 0, message = "Stop loss percent must be between -100 and 0")
    private Integer stopLossPercent = -50; // Default: -50%

    @NotNull(message = "Take profit levels are required")
    @Size(min = 1, max = 5, message = "Must specify 1-5 take profit levels")
    private List<@Positive Integer> takeProfitLevels; // e.g., [70, 130]

    @Min(value = 10, message = "Flat bet percent must be at least 10")
    @Max(value = 50, message = "Flat bet percent cannot exceed 50")
    private Integer flatBetPercent = 30; // Default: 30%

    @Min(value = 10, message = "Max spins must be at least 10")
    @Max(value = 200, message = "Max spins cannot exceed 200")
    private Integer maxSpins = 150;

    @Min(value = 10, message = "Max duration must be at least 10 minutes")
    @Max(value = 50, message = "Max duration cannot exceed 50 minutes")
    private Integer maxDurationMinutes = 120;

    private String notes;
}
