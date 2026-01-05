package com.revolution.tools.roulette.service;

import com.revolution.tools.roulette.dto.response.BetSlipAnalysisResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for AI-powered bet slip screenshot analysis using Claude Vision.
 */
public interface BetSlipAnalyzerService {

    /**
     * Analyze a bet slip screenshot and extract bet details.
     *
     * @param screenshot Bet slip image file (PNG, JPG, etc.)
     * @return Analysis response with extracted bet items
     */
    BetSlipAnalysisResponse analyzeScreenshot(MultipartFile screenshot);

    /**
     * Analyze screenshot and automatically record the bet.
     *
     * @param sessionId Session ID
     * @param screenshot Bet slip image
     * @return Analysis response with bet ID
     */
    BetSlipAnalysisResponse analyzeAndRecordBet(String sessionId, MultipartFile screenshot);
}
