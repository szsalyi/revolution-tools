package com.revolution.tools.roulette.entity;

import com.revolution.tools.roulette.enums.RuleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Custom betting rule based on observed game patterns.
 * Flexible rule engine supporting various pattern types.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "custom_rules")
public class CustomRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Rule name (e.g., "32 Adjacent Rule", "30-3 Pair")
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Description of the pattern
     */
    @Column(length = 500)
    private String description;

    /**
     * Rule type (ADJACENT, PAIR, SEQUENCE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    /**
     * Trigger number(s) - when to activate this rule.
     * For ADJACENT: last number that triggers rule
     * For PAIR: number that suggests another
     * Stored as CSV: "32" or "30,3"
     */
    @Column(nullable = false, length = 200)
    private String triggerNumbers;

    /**
     * Suggested numbers to bet on.
     * Stored as CSV: "30,34" or "3"
     */
    @Column(nullable = false, length = 200)
    private String suggestedNumbers;

    /**
     * Configuration JSON for complex rules.
     * For ADJACENT: {"offset": [-2, 2]}
     * For SEQUENCE: {"lookback": 3, "pattern": [30, 3, 15]}
     */
    @Column(columnDefinition = "TEXT")
    private String configJson;

    /**
     * Confidence level (1-100).
     * Higher = more reliable pattern.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer confidence = 50;

    /**
     * Number of times this rule was triggered and result was correct.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer hitCount = 0;

    /**
     * Total number of times this rule was triggered.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer totalTriggers = 0;

    /**
     * Whether this rule is active.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Created timestamp.
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Last updated timestamp.
     */
    private LocalDateTime updatedAt;

    /**
     * Calculate hit rate (success percentage).
     */
    public Double getHitRate() {
        if (totalTriggers == 0) {
            return 0.0;
        }
        return (hitCount * 100.0) / totalTriggers;
    }

    /**
     * Increment trigger count and optionally hit count.
     */
    public void recordTrigger(boolean wasHit) {
        this.totalTriggers++;
        if (wasHit) {
            this.hitCount++;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
