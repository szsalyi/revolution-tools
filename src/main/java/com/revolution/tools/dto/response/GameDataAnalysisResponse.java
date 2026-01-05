package com.revolution.tools.dto.response;

import com.revolution.tools.dto.request.GameDataAnalysisRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for game data analysis using Claude AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDataAnalysisResponse {

    private String sessionId;

    private GameDataAnalysisRequest.AnalysisType analysisType;

    /**
     * AI-generated analysis summary
     */
    private String summary;

    /**
     * Detailed insights from Claude
     */
    private List<Insight> insights;

    /**
     * Detected patterns
     */
    private List<Pattern> patterns;

    /**
     * Recommendations based on analysis
     */
    private List<String> recommendations;

    /**
     * Statistical summary if requested
     */
    private Map<String, Object> statistics;

    /**
     * Confidence score for the analysis (0.0 - 1.0)
     */
    private Double confidenceScore;

    /**
     * Timestamp of analysis
     */
    private LocalDateTime timestamp;

    /**
     * Token usage information
     */
    private TokenUsage tokenUsage;

    /**
     * Success status
     */
    private Boolean success;

    /**
     * Error message if any
     */
    private String errorMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Insight {
        private String category;
        private String description;
        private String severity; // LOW, MEDIUM, HIGH
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pattern {
        private String name;
        private String description;
        private Integer occurrences;
        private Double significance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
    }
}
