package com.revolution.tools.roulette.dto.response;

import com.revolution.tools.roulette.enums.AlertSeverity;
import com.revolution.tools.roulette.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response containing alert information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;
    private Long sessionId;
    private AlertType alertType;
    private AlertSeverity severity;
    private String message;
    private LocalDateTime timestamp;
    private Boolean acknowledged;
    private LocalDateTime acknowledgedAt;
    private String contextData;
}
