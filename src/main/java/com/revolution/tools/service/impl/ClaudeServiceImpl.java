package com.revolution.tools.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolution.tools.client.ClaudeApiClient;
import com.revolution.tools.config.ClaudeProperties;
import com.revolution.tools.dto.request.ClaudeAnalysisRequest;
import com.revolution.tools.dto.request.GameDataAnalysisRequest;
import com.revolution.tools.dto.response.ClaudeAnalysisResponse;
import com.revolution.tools.dto.response.GameDataAnalysisResponse;
import com.revolution.tools.exception.ClaudeAIException;
import com.revolution.tools.service.ClaudeService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ClaudeService for interacting with Anthropic's Claude AI.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "claude", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClaudeServiceImpl implements ClaudeService {

    private final ClaudeApiClient claudeApiClient;
    private final ClaudeProperties claudeProperties;
    private final ObjectMapper objectMapper;

    public ClaudeServiceImpl(ClaudeApiClient claudeApiClient,
                             ClaudeProperties claudeProperties,
                             ObjectMapper objectMapper) {
        this.claudeApiClient = claudeApiClient;
        this.claudeProperties = claudeProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "claudeAI", fallbackMethod = "analyzeFallback")
    @Retry(name = "claudeAI")
    public ClaudeAnalysisResponse analyze(ClaudeAnalysisRequest request) {
        log.info("Sending analysis request to Claude AI");

        try {
            // Build messages list
            List<ClaudeApiClient.ClaudeApiRequest.Message> messages = new ArrayList<>();

            // Add conversation history if provided
            if (request.getConversationHistory() != null && !request.getConversationHistory().isEmpty()) {
                for (ClaudeAnalysisRequest.ConversationMessage msg : request.getConversationHistory()) {
                    messages.add(ClaudeApiClient.ClaudeApiRequest.Message.builder()
                            .role(msg.getRole().toLowerCase())
                            .content(msg.getContent())
                            .build());
                }
            }

            // Add current user prompt
            messages.add(ClaudeApiClient.ClaudeApiRequest.Message.builder()
                    .role("user")
                    .content(request.getPrompt())
                    .build());

            // Build request
            ClaudeApiClient.ClaudeApiRequest.ClaudeApiRequestBuilder apiRequestBuilder =
                    ClaudeApiClient.ClaudeApiRequest.builder()
                            .model(request.getModel() != null ? request.getModel() : claudeProperties.getModel())
                            .maxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : claudeProperties.getMaxTokens())
                            .temperature(request.getTemperature() != null ? request.getTemperature() : claudeProperties.getTemperature())
                            .messages(messages);

            // Add system prompt if provided
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
                apiRequestBuilder.system(List.of(
                        ClaudeApiClient.ClaudeApiRequest.SystemMessage.builder()
                                .type("text")
                                .text(request.getSystemPrompt())
                                .build()
                ));
            }

            // Call Claude API
            ClaudeApiClient.ClaudeApiResponse apiResponse = claudeApiClient.sendMessage(apiRequestBuilder.build());

            // Extract text content from response
            String content = apiResponse.getContent().stream()
                    .filter(block -> "text".equals(block.getType()))
                    .map(ClaudeApiClient.ClaudeApiResponse.Content::getText)
                    .collect(Collectors.joining("\n"));

            // Build response
            return ClaudeAnalysisResponse.builder()
                    .content(content)
                    .model(apiResponse.getModel())
                    .inputTokens(apiResponse.getUsage().getInputTokens())
                    .outputTokens(apiResponse.getUsage().getOutputTokens())
                    .totalTokens(apiResponse.getUsage().getInputTokens() + apiResponse.getUsage().getOutputTokens())
                    .stopReason(apiResponse.getStopReason())
                    .timestamp(LocalDateTime.now())
                    .requestId(apiResponse.getId())
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Error calling Claude API: {}", e.getMessage(), e);
            throw new ClaudeAIException("Failed to analyze with Claude AI: " + e.getMessage(), e);
        }
    }

    @Override
    @CircuitBreaker(name = "claudeAI", fallbackMethod = "analyzeGameDataFallback")
    @Retry(name = "claudeAI")
    public GameDataAnalysisResponse analyzeGameData(GameDataAnalysisRequest request) {
        log.info("Analyzing game data for session: {} with analysis type: {}",
                request.getSessionId(), request.getAnalysisType());

        try {
            // Build comprehensive prompt for game data analysis
            String prompt = buildGameDataAnalysisPrompt(request);

            // Create Claude request
            ClaudeAnalysisRequest claudeRequest = ClaudeAnalysisRequest.builder()
                    .prompt(prompt)
                    .systemPrompt("You are an expert game data analyst specializing in Evolution gaming. " +
                            "Provide detailed, data-driven insights with specific recommendations. " +
                            "Format your response as JSON with fields: summary, insights, patterns, and recommendations.")
                    .model(claudeProperties.getModel())
                    .maxTokens(claudeProperties.getMaxTokens())
                    .temperature(0.3) // Lower temperature for more focused analysis
                    .build();

            // Get Claude's analysis
            ClaudeAnalysisResponse claudeResponse = analyze(claudeRequest);

            // Parse Claude's response into structured data
            return parseGameDataAnalysisResponse(claudeResponse, request);

        } catch (Exception e) {
            log.error("Error analyzing game data: {}", e.getMessage(), e);

            return GameDataAnalysisResponse.builder()
                    .sessionId(request.getSessionId())
                    .analysisType(request.getAnalysisType())
                    .success(false)
                    .errorMessage("Failed to analyze game data: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public boolean testConnection() {
        try {
            log.info("Testing connection to Claude API");

            ClaudeAnalysisRequest testRequest = ClaudeAnalysisRequest.builder()
                    .prompt("Hello! Please respond with 'OK' if you receive this message.")
                    .maxTokens(10)
                    .build();

            ClaudeAnalysisResponse response = analyze(testRequest);

            return response.getSuccess() != null && response.getSuccess();
        } catch (Exception e) {
            log.error("Claude API connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Builds a comprehensive prompt for game data analysis.
     */
    private String buildGameDataAnalysisPrompt(GameDataAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Analysis Type: ").append(request.getAnalysisType().getDescription()).append("\n\n");

        if (request.getGameData() != null && !request.getGameData().isEmpty()) {
            try {
                prompt.append("Game Data:\n");
                prompt.append(objectMapper.writeValueAsString(request.getGameData()));
                prompt.append("\n\n");
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize game data: {}", e.getMessage());
            }
        }

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            prompt.append("Specific Questions:\n");
            for (int i = 0; i < request.getQuestions().size(); i++) {
                prompt.append(i + 1).append(". ").append(request.getQuestions().get(i)).append("\n");
            }
            prompt.append("\n");
        }

        if (request.getIncludeStatistics() != null && request.getIncludeStatistics()) {
            prompt.append("Please include detailed statistical analysis.\n\n");
        }

        if (request.getIncludeInsights() != null && request.getIncludeInsights()) {
            prompt.append("Please provide actionable insights and recommendations.\n\n");
        }

        prompt.append("Please analyze this game data and provide:\n");
        prompt.append("1. A comprehensive summary\n");
        prompt.append("2. Key insights with severity ratings (LOW, MEDIUM, HIGH)\n");
        prompt.append("3. Detected patterns with significance scores\n");
        prompt.append("4. Actionable recommendations\n");

        return prompt.toString();
    }

    /**
     * Parses Claude's response into structured game data analysis response.
     */
    private GameDataAnalysisResponse parseGameDataAnalysisResponse(
            ClaudeAnalysisResponse claudeResponse,
            GameDataAnalysisRequest request) {

        // For now, provide a simple structured response
        // In production, you might want to parse JSON from Claude's response

        List<GameDataAnalysisResponse.Insight> insights = new ArrayList<>();
        insights.add(GameDataAnalysisResponse.Insight.builder()
                .category("AI Analysis")
                .description(claudeResponse.getContent())
                .severity("MEDIUM")
                .confidence(0.85)
                .build());

        return GameDataAnalysisResponse.builder()
                .sessionId(request.getSessionId())
                .analysisType(request.getAnalysisType())
                .summary(claudeResponse.getContent())
                .insights(insights)
                .patterns(new ArrayList<>())
                .recommendations(List.of("Review the detailed analysis above"))
                .statistics(new HashMap<>())
                .confidenceScore(0.85)
                .timestamp(LocalDateTime.now())
                .tokenUsage(GameDataAnalysisResponse.TokenUsage.builder()
                        .inputTokens(claudeResponse.getInputTokens())
                        .outputTokens(claudeResponse.getOutputTokens())
                        .totalTokens(claudeResponse.getTotalTokens())
                        .build())
                .success(true)
                .build();
    }

    /**
     * Fallback method for analyze when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    private ClaudeAnalysisResponse analyzeFallback(ClaudeAnalysisRequest request, Exception e) {
        log.error("Circuit breaker open for Claude AI analysis", e);

        return ClaudeAnalysisResponse.builder()
                .content("Claude AI service is temporarily unavailable. Please try again later.")
                .success(false)
                .errorMessage("Service temporarily unavailable: " + e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Fallback method for analyzeGameData when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    private GameDataAnalysisResponse analyzeGameDataFallback(GameDataAnalysisRequest request, Exception e) {
        log.error("Circuit breaker open for game data analysis", e);

        return GameDataAnalysisResponse.builder()
                .sessionId(request.getSessionId())
                .analysisType(request.getAnalysisType())
                .summary("Analysis service temporarily unavailable")
                .success(false)
                .errorMessage("Service temporarily unavailable: " + e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
