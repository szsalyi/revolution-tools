package com.revolution.tools.roulette.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolution.tools.dto.request.ClaudeAnalysisRequest;
import com.revolution.tools.dto.response.ClaudeAnalysisResponse;
import com.revolution.tools.service.ClaudeService;
import com.revolution.tools.roulette.dto.BetItem;
import com.revolution.tools.roulette.dto.request.PlaceBetRequest;
import com.revolution.tools.roulette.dto.response.BetSlipAnalysisResponse;
import com.revolution.tools.roulette.dto.response.BetValidationResponse;
import com.revolution.tools.roulette.enums.BetType;
import com.revolution.tools.roulette.service.BetSlipAnalyzerService;
import com.revolution.tools.roulette.service.RouletteSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * AI-powered bet slip analyzer using Claude Vision.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BetSlipAnalyzerServiceImpl implements BetSlipAnalyzerService {

    private final ClaudeService claudeService;
    private final RouletteSessionService rouletteSessionService;
    private final ObjectMapper objectMapper;

    @Override
    public BetSlipAnalysisResponse analyzeScreenshot(MultipartFile screenshot) {
        log.info("Analyzing bet slip screenshot: {} (size: {} bytes)",
                screenshot.getOriginalFilename(), screenshot.getSize());

        try {
            // Convert image to base64
            byte[] imageBytes = screenshot.getBytes();
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);

            // Create AI prompt for bet slip analysis
            String prompt = buildBetSlipAnalysisPrompt();

            // Call Claude AI with vision
            ClaudeAnalysisRequest request = ClaudeAnalysisRequest.builder()
                    .prompt(prompt)
                    .systemPrompt("You are an expert at reading roulette bet slips and extracting structured data.")
                    .maxTokens(1000)
                    .temperature(0.0) // Deterministic for data extraction
                    .build();

            ClaudeAnalysisResponse aiResponse = claudeService.analyze(request);

            log.info("Claude AI analysis complete. Response length: {}", aiResponse.getContent().length());

            // Parse AI response into BetItems
            BetSlipAnalysisResponse analysis = parseBetSlipFromAI(aiResponse.getContent());
            analysis.setRawAiResponse(aiResponse.getContent());
            analysis.setAiConfidence(calculateConfidence(analysis));

            log.info("Bet slip analysis: detected={}, items={}, totalStake={}, confidence={}",
                    analysis.getBetDetected(), analysis.getBetItems().size(),
                    analysis.getTotalStake(), analysis.getAiConfidence());

            return analysis;

        } catch (Exception e) {
            log.error("Failed to analyze bet slip screenshot", e);
            return BetSlipAnalysisResponse.builder()
                    .betDetected(false)
                    .analysisNotes("Failed to analyze screenshot: " + e.getMessage())
                    .aiConfidence(0.0)
                    .warnings(List.of("AI analysis failed - please try again or enter bet manually"))
                    .build();
        }
    }

    @Override
    public BetSlipAnalysisResponse analyzeAndRecordBet(String sessionId, MultipartFile screenshot) {
        log.info("Analyzing screenshot and recording bet for session: {}", sessionId);

        // Analyze screenshot
        BetSlipAnalysisResponse analysis = analyzeScreenshot(screenshot);

        if (!analysis.getBetDetected() || analysis.getBetItems() == null || analysis.getBetItems().isEmpty()) {
            log.warn("No bet detected in screenshot for session: {}", sessionId);
            analysis.setBetRecorded(false);
            return analysis;
        }

        try {
            // Create bet request from analyzed items
            PlaceBetRequest betRequest = PlaceBetRequest.builder()
                    .bets(analysis.getBetItems())
                    .build();

            // Record the bet
            BetValidationResponse betResponse = rouletteSessionService.placeBet(sessionId, betRequest);

            analysis.setBetRecorded(true);
            analysis.setBetId(betResponse.getBetId());

            if (!betResponse.getValid()) {
                List<String> warnings = new ArrayList<>(analysis.getWarnings() != null ? analysis.getWarnings() : new ArrayList<>());
                warnings.addAll(betResponse.getViolations());
                analysis.setWarnings(warnings);
            }

            log.info("Bet recorded successfully from screenshot: betId={}", betResponse.getBetId());

        } catch (Exception e) {
            log.error("Failed to record bet from screenshot", e);
            analysis.setBetRecorded(false);
            List<String> warnings = new ArrayList<>(analysis.getWarnings() != null ? analysis.getWarnings() : new ArrayList<>());
            warnings.add("Failed to record bet: " + e.getMessage());
            analysis.setWarnings(warnings);
        }

        return analysis;
    }

    /**
     * Build AI prompt for bet slip analysis.
     */
    private String buildBetSlipAnalysisPrompt() {
        return """
                Analyze this roulette bet slip screenshot and extract the betting information.

                Look for:
                1. Roulette numbers being bet on (0-36)
                2. Stake amount for each number (may be the same or different per number)
                3. Total stake amount

                Return your analysis as a JSON object with this EXACT format:
                {
                  "numbers": [4, 22, 14, 34],
                  "stakes": [2.00, 2.00, 0.50, 0.50],
                  "totalStake": 5.00,
                  "confidence": "high",
                  "notes": "Brief description of what you see"
                }

                Important:
                - numbers array should contain only valid roulette numbers (0-36)
                - stakes array should be parallel to numbers array (same length)
                - If all stakes are the same, you can still list them individually
                - totalStake should equal the sum of all stakes
                - confidence should be: "high", "medium", or "low"
                - If you cannot clearly read the bet slip, return confidence as "low" and explain in notes

                Return ONLY the JSON object, no additional text.
                """;
    }

    /**
     * Parse Claude AI response into BetSlipAnalysisResponse.
     */
    private BetSlipAnalysisResponse parseBetSlipFromAI(String aiResponse) {
        try {
            // Extract JSON from response (Claude might add some text before/after)
            String jsonContent = extractJsonFromResponse(aiResponse);

            // Parse JSON
            JsonNode root = objectMapper.readTree(jsonContent);

            // Extract numbers and stakes
            List<Integer> numbers = new ArrayList<>();
            JsonNode numbersNode = root.get("numbers");
            if (numbersNode != null && numbersNode.isArray()) {
                for (JsonNode num : numbersNode) {
                    numbers.add(num.asInt());
                }
            }

            List<BigDecimal> stakes = new ArrayList<>();
            JsonNode stakesNode = root.get("stakes");
            if (stakesNode != null && stakesNode.isArray()) {
                for (JsonNode stake : stakesNode) {
                    stakes.add(BigDecimal.valueOf(stake.asDouble()));
                }
            }

            BigDecimal totalStake = root.has("totalStake") ?
                    BigDecimal.valueOf(root.get("totalStake").asDouble()) : BigDecimal.ZERO;

            String confidence = root.has("confidence") ? root.get("confidence").asText() : "low";
            String notes = root.has("notes") ? root.get("notes").asText() : "Analysis complete";

            // Build BetItems
            List<BetItem> betItems = new ArrayList<>();
            for (int i = 0; i < Math.min(numbers.size(), stakes.size()); i++) {
                BetItem item = new BetItem(
                        numbers.get(i),
                        stakes.get(i),
                        BetType.UNKNOWN // Source unknown from screenshot
                );
                betItems.add(item);
            }

            boolean betDetected = !betItems.isEmpty();

            return BetSlipAnalysisResponse.builder()
                    .betDetected(betDetected)
                    .betItems(betItems)
                    .totalStake(totalStake)
                    .analysisNotes(notes)
                    .betRecorded(false)
                    .warnings(new ArrayList<>())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
            return BetSlipAnalysisResponse.builder()
                    .betDetected(false)
                    .analysisNotes("Failed to parse AI response: " + e.getMessage())
                    .aiConfidence(0.0)
                    .warnings(List.of("Could not parse bet slip - please try again"))
                    .build();
        }
    }

    /**
     * Extract JSON content from AI response (handles extra text).
     */
    private String extractJsonFromResponse(String response) {
        // Find JSON object boundaries
        int startIdx = response.indexOf('{');
        int endIdx = response.lastIndexOf('}');

        if (startIdx >= 0 && endIdx > startIdx) {
            return response.substring(startIdx, endIdx + 1);
        }

        return response; // Return as-is if no JSON boundaries found
    }

    /**
     * Calculate confidence score from analysis.
     */
    private Double calculateConfidence(BetSlipAnalysisResponse analysis) {
        if (!analysis.getBetDetected() || analysis.getBetItems() == null || analysis.getBetItems().isEmpty()) {
            return 0.0;
        }

        // Base confidence on number of items and total stake consistency
        double confidence = 0.7; // Base confidence

        // Boost if we have reasonable number of items (3-15 is typical)
        if (analysis.getBetItems().size() >= 3 && analysis.getBetItems().size() <= 15) {
            confidence += 0.2;
        }

        // Boost if total stake matches sum of individual stakes
        BigDecimal calculatedTotal = analysis.getBetItems().stream()
                .map(BetItem::getStake)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (analysis.getTotalStake() != null &&
                analysis.getTotalStake().compareTo(calculatedTotal) == 0) {
            confidence += 0.1;
        }

        return Math.min(confidence, 1.0);
    }
}
