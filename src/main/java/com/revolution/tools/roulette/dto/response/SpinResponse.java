package com.revolution.tools.roulette.dto.response;

import com.revolution.tools.roulette.enums.RouletteColor;
import com.revolution.tools.roulette.enums.RouletteSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a roulette spin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinResponse {
    private Long spinId;
    private String sessionId;
    private Integer spinNumber;
    private RouletteColor color;
    private RouletteSection section;
    private Integer sequenceNumber;
    private LocalDateTime timestamp;
}
