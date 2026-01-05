package com.revolution.tools.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.revolution.tools.config.ClaudeProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * HTTP client for Anthropic's Claude API.
 * Uses RestTemplate to make direct API calls.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "claude", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClaudeApiClient {

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestTemplate restTemplate;
    private final ClaudeProperties claudeProperties;

    public ClaudeApiClient(RestTemplate restTemplate, ClaudeProperties claudeProperties) {
        this.restTemplate = restTemplate;
        this.claudeProperties = claudeProperties;
    }

    /**
     * Sends a message to Claude and returns the response.
     */
    public ClaudeApiResponse sendMessage(ClaudeApiRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", claudeProperties.getApiKey());
            headers.set("anthropic-version", ANTHROPIC_VERSION);

            HttpEntity<ClaudeApiRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Sending request to Claude API: model={}, max_tokens={}",
                    request.getModel(), request.getMaxTokens());

            ResponseEntity<ClaudeApiResponse> response = restTemplate.postForEntity(
                    ANTHROPIC_API_URL,
                    entity,
                    ClaudeApiResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Error calling Claude API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Claude API: " + e.getMessage(), e);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaudeApiRequest {
        private String model;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        private Double temperature;

        private List<Message> messages;

        private List<SystemMessage> system;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Message {
            private String role;
            private String content;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SystemMessage {
            private String type;
            private String text;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaudeApiResponse {
        private String id;

        private String type;

        private String role;

        private List<Content> content;

        private String model;

        @JsonProperty("stop_reason")
        private String stopReason;

        @JsonProperty("stop_sequence")
        private String stopSequence;

        private Usage usage;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Content {
            private String type;
            private String text;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Usage {
            @JsonProperty("input_tokens")
            private Integer inputTokens;

            @JsonProperty("output_tokens")
            private Integer outputTokens;
        }
    }
}
