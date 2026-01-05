package com.revolution.tools.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for Claude AI analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeAnalysisRequest {

    @NotBlank(message = "Prompt is required")
    @Size(min = 1, max = 10000, message = "Prompt must be between 1 and 10000 characters")
    private String prompt;

    /**
     * Optional context or additional data for analysis
     */
    private Map<String, Object> context;

    /**
     * Optional system prompt to guide Claude's behavior
     */
    private String systemPrompt;

    /**
     * Optional model override (defaults to configured model)
     */
    private String model;

    /**
     * Optional max tokens override
     */
    private Integer maxTokens;

    /**
     * Optional temperature override (0.0 - 1.0)
     */
    private Double temperature;

    /**
     * Optional conversation history for multi-turn conversations
     */
    private List<ConversationMessage> conversationHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationMessage {
        private String role; // "user" or "assistant"
        private String content;
    }
}
