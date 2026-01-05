package com.revolution.tools.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Claude AI integration.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "claude")
public class ClaudeProperties {

    /**
     * Claude API key from environment variable
     */
    private String apiKey;

    /**
     * Default model to use for Claude API calls
     * Options: claude-3-5-sonnet-20241022, claude-3-opus-20240229, claude-3-haiku-20240307
     */
    private String model = "claude-3-5-sonnet-20241022";

    /**
     * Maximum tokens to generate in response
     */
    private Integer maxTokens = 4096;

    /**
     * Temperature for response generation (0.0 - 1.0)
     */
    private Double temperature = 0.7;

    /**
     * API request timeout in seconds
     */
    private Integer timeoutSeconds = 60;

    /**
     * Enable or disable Claude AI features
     */
    private Boolean enabled = true;
}
