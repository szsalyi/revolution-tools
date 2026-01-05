package com.revolution.tools.roulette.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for pattern analysis and betting suggestions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternSuggestionResponse {
    private String sessionId;
    private List<Integer> hotNumbers;
    private List<Integer> missingNumbers;
    private List<Integer> suggestedNumbers;
    private String dominantSection;
    private List<String> detectedPatterns;
}
