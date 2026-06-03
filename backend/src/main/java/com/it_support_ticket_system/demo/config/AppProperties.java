package com.it_support_ticket_system.demo.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Cors cors = new Cors();
    private final Ai ai = new Ai();

    public Cors getCors() {
        return cors;
    }

    public Ai getAi() {
        return ai;
    }

    public static class Cors {

        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:3000", "http://localhost:5173"));

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Ai {

        @NotBlank
        private String baseUrl = "http://localhost:8000";

        @NotBlank
        private String predictPath = "/predict";

        @Min(1)
        private int timeoutSeconds = 5;

        @NotBlank
        private String fallbackCategory = "Other";

        @Min(1)
        private int topPredictionsLimit = 3;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getPredictPath() {
            return predictPath;
        }

        public void setPredictPath(String predictPath) {
            this.predictPath = predictPath;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public String getFallbackCategory() {
            return fallbackCategory;
        }

        public void setFallbackCategory(String fallbackCategory) {
            this.fallbackCategory = fallbackCategory;
        }

        public int getTopPredictionsLimit() {
            return topPredictionsLimit;
        }

        public void setTopPredictionsLimit(int topPredictionsLimit) {
            this.topPredictionsLimit = topPredictionsLimit;
        }
    }
}
