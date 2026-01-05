package com.revolution.tools.roulette.entity;

import com.revolution.tools.roulette.enums.AlertSeverity;
import com.revolution.tools.roulette.enums.AlertType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an alert/notification during a roulette session.
 */
@Entity
@Table(name = "roulette_alerts", indexes = {
    @Index(name = "idx_alert_session_id", columnList = "session_id"),
    @Index(name = "idx_alert_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouletteAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "alert_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(name = "severity", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "acknowledged", nullable = false)
    private Boolean acknowledged;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "auto_dismissed", nullable = false)
    private Boolean autoDismissed;

    @Column(name = "context_data", length = 2000)
    private String contextData; // JSON data for additional context

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (acknowledged == null) {
            acknowledged = false;
        }
        if (autoDismissed == null) {
            autoDismissed = false;
        }
    }

    /**
     * Acknowledges this alert.
     */
    public void acknowledge() {
        this.acknowledged = true;
        this.acknowledgedAt = LocalDateTime.now();
    }
}
