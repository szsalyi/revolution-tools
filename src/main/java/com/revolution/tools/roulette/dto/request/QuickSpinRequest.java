package com.revolution.tools.roulette.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal request to quickly record a spin result.
 * Production-ready: Just send the winning number.
 *
 * Example:
 * POST /api/roulette/sessions/{sessionId}/spins/quick
 * { "number": 17 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickSpinRequest {

    /**
     * The winning number (0-36).
     */
    @NotNull(message = "Number is required")
    @Min(value = 0, message = "Number must be between 0 and 36")
    @Max(value = 36, message = "Number must be between 0 and 36")
    private Integer number;
}
