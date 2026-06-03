package com.it_support_ticket_system.demo.ai;

import java.util.List;

public record AiPredictionResult(
    String predictedCategory,
    double confidence,
    List<TopPrediction> topPredictions,
    boolean failed,
    String errorMessage
) {
    public AiPredictionResult {
        topPredictions = List.copyOf(topPredictions);
    }

    public static AiPredictionResult success(
        String predictedCategory,
        double confidence,
        List<TopPrediction> topPredictions
    ) {
        return new AiPredictionResult(predictedCategory, confidence, topPredictions, false, null);
    }

    public static AiPredictionResult failure(String predictedCategory, String errorMessage) {
        return new AiPredictionResult(predictedCategory, 0.0, List.of(), true, errorMessage);
    }

    public record TopPrediction(
        String category,
        double probability
    ) {
    }
}
