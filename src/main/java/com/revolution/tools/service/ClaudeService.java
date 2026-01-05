package com.revolution.tools.service;

import com.revolution.tools.dto.request.ClaudeAnalysisRequest;
import com.revolution.tools.dto.request.GameDataAnalysisRequest;
import com.revolution.tools.dto.response.ClaudeAnalysisResponse;
import com.revolution.tools.dto.response.GameDataAnalysisResponse;

/**
 * Service interface for Claude AI integration.
 */
public interface ClaudeService {

    /**
     * Sends a prompt to Claude and returns the response.
     *
     * @param request Claude analysis request
     * @return Claude analysis response
     */
    ClaudeAnalysisResponse analyze(ClaudeAnalysisRequest request);

    /**
     * Analyzes game data using Claude AI.
     *
     * @param request Game data analysis request
     * @return Game data analysis response with insights
     */
    GameDataAnalysisResponse analyzeGameData(GameDataAnalysisRequest request);

    /**
     * Tests the connection to Claude API.
     *
     * @return true if connection is successful
     */
    boolean testConnection();
}
