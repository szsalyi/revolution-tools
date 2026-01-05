package com.revolution.tools.roulette.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to record a roulette spin result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordSpinRequest {

    @NotNull(message = "Spin number is required")
    @Min(value = 0, message = "Spin number must be between 0 and 36")
    @Max(value = 36, message = "Spin number must be between 0 and 36")
    private Integer spinNumber;
}
