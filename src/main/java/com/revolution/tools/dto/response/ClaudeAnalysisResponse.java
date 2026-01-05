package com.revolution.tools.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for Claude AI analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeAnalysisResponse {

    /**
     * The AI-generated response content
     */
    private String content;

    /**
     * Model used for generation
     */
    private String model;

    /**
     * Number of tokens used in the request
     */
    private Integer inputTokens;

    /**
     * Number of tokens generated in the response
     */
    private Integer outputTokens;

    /**
     * Total tokens used (input + output)
     */
    private Integer totalTokens;

    /**
     * Stop reason: "end_turn", "max_tokens", "stop_sequence"
     */
    private String stopReason;

    /**
     * Response timestamp
     */
    private LocalDateTime timestamp;

    /**
     * Request ID for tracking
     */
    private String requestId;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Indicates if the response was successful
     */
    private Boolean success;

    /**
     * Error message if any
     */
    private String errorMessage;
}
