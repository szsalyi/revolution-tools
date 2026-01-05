package com.revolution.tools.controller;

import com.revolution.tools.dto.request.ClaudeAnalysisRequest;
import com.revolution.tools.dto.request.GameDataAnalysisRequest;
import com.revolution.tools.dto.response.ClaudeAnalysisResponse;
import com.revolution.tools.dto.response.GameDataAnalysisResponse;
import com.revolution.tools.service.ClaudeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Claude AI integration endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/claude")
@ConditionalOnProperty(prefix = "claude", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClaudeController {

    private final ClaudeService claudeService;

    public ClaudeController(ClaudeService claudeService) {
        this.claudeService = claudeService;
    }

    /**
     * Tests the connection to Claude API.
     *
     * @return Connection test result
     */
    @GetMapping("/test")
    public ResponseEntity<ConnectionTestResponse> testConnection() {
        log.info("GET /api/claude/test - Testing Claude API connection");

        boolean connected = claudeService.testConnection();

        ConnectionTestResponse response = new ConnectionTestResponse(
                connected,
                connected ? "Successfully connected to Claude AI" : "Failed to connect to Claude AI"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes text or data using Claude AI.
     *
     * @param request Claude analysis request
     * @return Claude analysis response
     */
    @PostMapping("/analyze")
    public ResponseEntity<ClaudeAnalysisResponse> analyze(@Valid @RequestBody ClaudeAnalysisRequest request) {
        log.info("POST /api/claude/analyze - Analyzing with Claude AI");

        ClaudeAnalysisResponse response = claudeService.analyze(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Analyzes game data using Claude AI for insights and recommendations.
     *
     * @param request Game data analysis request
     * @return Game data analysis response with insights
     */
    @PostMapping("/analyze-game-data")
    public ResponseEntity<GameDataAnalysisResponse> analyzeGameData(
            @Valid @RequestBody GameDataAnalysisRequest request) {
        log.info("POST /api/claude/analyze-game-data - Analyzing game data for session: {}",
                request.getSessionId());

        GameDataAnalysisResponse response = claudeService.analyzeGameData(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Gets available analysis types for game data.
     *
     * @return List of available analysis types
     */
    @GetMapping("/analysis-types")
    public ResponseEntity<AnalysisTypesResponse> getAnalysisTypes() {
        log.info("GET /api/claude/analysis-types - Getting available analysis types");

        AnalysisTypesResponse response = new AnalysisTypesResponse(
                java.util.Arrays.stream(GameDataAnalysisRequest.AnalysisType.values())
                        .map(type -> new AnalysisTypeInfo(
                                type.name(),
                                type.getDescription()
                        ))
                        .toList()
        );

        return ResponseEntity.ok(response);
    }

    @Data
    @AllArgsConstructor
    public static class ConnectionTestResponse {
        private boolean connected;
        private String message;
    }

    @Data
    @AllArgsConstructor
    public static class AnalysisTypesResponse {
        private java.util.List<AnalysisTypeInfo> types;
    }

    @Data
    @AllArgsConstructor
    public static class AnalysisTypeInfo {
        private String name;
        private String description;
    }
}
