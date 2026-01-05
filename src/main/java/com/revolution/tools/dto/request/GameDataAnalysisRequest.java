package com.revolution.tools.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for analyzing game data using Claude AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDataAnalysisRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotNull(message = "Analysis type is required")
    private AnalysisType analysisType;

    /**
     * Raw game data to analyze
     */
    private List<Map<String, Object>> gameData;

    /**
     * Specific questions to ask Claude about the game data
     */
    private List<String> questions;

    /**
     * Additional context or metadata
     */
    private Map<String, Object> context;

    /**
     * Include detailed statistics in analysis
     */
    private Boolean includeStatistics;

    /**
     * Include AI-powered insights and recommendations
     */
    private Boolean includeInsights;

    public enum AnalysisType {
        PATTERN_DETECTION("Detect patterns in game behavior"),
        STRATEGY_ANALYSIS("Analyze gameplay strategy"),
        RISK_ASSESSMENT("Assess risk patterns"),
        OUTCOME_PREDICTION("Predict likely outcomes"),
        ANOMALY_DETECTION("Detect anomalies or unusual behavior"),
        PERFORMANCE_ANALYSIS("Analyze performance metrics"),
        GENERAL_INSIGHTS("General insights and recommendations");

        private final String description;

        AnalysisType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
