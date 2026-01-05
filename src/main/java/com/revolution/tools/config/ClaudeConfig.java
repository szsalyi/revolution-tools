package com.revolution.tools.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Claude AI.
 * API client is configured via ClaudeApiClient component.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "claude", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClaudeConfig {

    public ClaudeConfig(ClaudeProperties claudeProperties) {
        log.info("Claude AI integration enabled with model: {}", claudeProperties.getModel());

        if (claudeProperties.getApiKey() == null || claudeProperties.getApiKey().isEmpty()) {
            log.warn("Claude API key not configured. Set CLAUDE_API_KEY environment variable.");
        }
    }
}
