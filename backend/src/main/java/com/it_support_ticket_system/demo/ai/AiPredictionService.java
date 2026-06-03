package com.it_support_ticket_system.demo.ai;

import com.it_support_ticket_system.demo.common.BadRequestException;
import com.it_support_ticket_system.demo.common.ExternalServiceException;
import com.it_support_ticket_system.demo.config.AppProperties;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AiPredictionService {

    private final AiPredictionClient aiPredictionClient;
    private final AppProperties appProperties;

    public AiPredictionService(AiPredictionClient aiPredictionClient, AppProperties appProperties) {
        this.aiPredictionClient = aiPredictionClient;
        this.appProperties = appProperties;
    }

    public AiPredictionResult predict(String text) {
        String normalizedText = trimToNull(text);
        if (normalizedText == null) {
            throw new BadRequestException("Text is required.");
        }

        AiPredictionResponse response = aiPredictionClient.predict(normalizedText);
        return validateAndMap(response);
    }

    private AiPredictionResult validateAndMap(AiPredictionResponse response) {
        String predictedCategory = trimToNull(response.predictedCategory());
        Double confidence = response.confidence();

        if (predictedCategory == null || confidence == null || confidence < 0 || confidence > 1) {
            throw new ExternalServiceException(AiPredictionClient.AI_INVALID_RESPONSE_MESSAGE);
        }

        List<AiPredictionResult.TopPrediction> topPredictions = normalizeTopPredictions(response.topPredictions());
        return AiPredictionResult.success(predictedCategory, confidence, topPredictions);
    }

    private List<AiPredictionResult.TopPrediction> normalizeTopPredictions(List<AiTopPredictionDto> topPredictions) {
        if (topPredictions == null) {
            return List.of();
        }

        return topPredictions.stream()
            .limit(appProperties.getAi().getTopPredictionsLimit())
            .map(this::toTopPrediction)
            .toList();
    }

    private AiPredictionResult.TopPrediction toTopPrediction(AiTopPredictionDto topPrediction) {
        String category = trimToNull(topPrediction.category());
        Double probability = topPrediction.probability();

        if (category == null || probability == null || probability < 0 || probability > 1) {
            throw new ExternalServiceException(AiPredictionClient.AI_INVALID_RESPONSE_MESSAGE);
        }

        return new AiPredictionResult.TopPrediction(category, probability);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
