package com.revolution.tools.roulette.dto.response;

import com.revolution.tools.roulette.enums.PatternType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response containing detected pattern information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternResponse {

    private PatternType type;
    private List<Integer> numbers;
    private Double confidence;
    private Integer validForNextSpins;
    private String description;
    private String recommendation;
}
