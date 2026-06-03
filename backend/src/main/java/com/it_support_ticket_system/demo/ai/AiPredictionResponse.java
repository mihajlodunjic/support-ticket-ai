package com.it_support_ticket_system.demo.ai;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record AiPredictionResponse(
    @JsonAlias("predicted_category")
    String predictedCategory,
    Double confidence,
    @JsonAlias("top_predictions")
    List<AiTopPredictionDto> topPredictions
) {
    public static AiPredictionResponse from(AiPredictionResult result) {
        List<AiTopPredictionDto> topPredictionDtos = result.topPredictions().stream()
            .map(prediction -> new AiTopPredictionDto(prediction.category(), prediction.probability()))
            .toList();

        return new AiPredictionResponse(result.predictedCategory(), result.confidence(), topPredictionDtos);
    }
}
