package com.it_support_ticket_system.demo.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.it_support_ticket_system.demo.common.AiServiceUnavailableException;
import com.it_support_ticket_system.demo.common.ExternalServiceException;
import com.it_support_ticket_system.demo.config.AppProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
public class AiPredictionClient {

    static final String AI_UNAVAILABLE_MESSAGE = "AI prediction service is unavailable.";
    static final String AI_REJECTED_MESSAGE = "AI prediction service rejected the request.";
    static final String AI_INVALID_RESPONSE_MESSAGE = "AI prediction service returned an invalid response.";

    private final WebClient aiWebClient;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public AiPredictionClient(WebClient aiWebClient, ObjectMapper objectMapper, AppProperties appProperties) {
        this.aiWebClient = aiWebClient;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    public AiPredictionResponse predict(String text) {
        try {
            AiPredictionResponse response = aiWebClient.post()
                .uri(appProperties.getAi().getPredictPath())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AiPredictionRequest(text))
                .exchangeToMono(this::handleResponse)
                .block();

            if (response == null) {
                throw new ExternalServiceException(AI_INVALID_RESPONSE_MESSAGE);
            }

            return response;
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (WebClientRequestException exception) {
            throw new AiServiceUnavailableException(AI_UNAVAILABLE_MESSAGE);
        } catch (RuntimeException exception) {
            throw new ExternalServiceException(AI_INVALID_RESPONSE_MESSAGE);
        }
    }

    private Mono<AiPredictionResponse> handleResponse(org.springframework.web.reactive.function.client.ClientResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(String.class)
                .map(this::parseResponseBody);
        }

        if (response.statusCode().is5xxServerError()) {
            return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .then(Mono.error(new AiServiceUnavailableException(AI_UNAVAILABLE_MESSAGE)));
        }

        if (response.statusCode().is4xxClientError()) {
            return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .then(Mono.error(new ExternalServiceException(AI_REJECTED_MESSAGE)));
        }

        return response.bodyToMono(String.class)
            .defaultIfEmpty("")
            .then(Mono.error(new ExternalServiceException(AI_INVALID_RESPONSE_MESSAGE)));
    }

    private AiPredictionResponse parseResponseBody(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, AiPredictionResponse.class);
        } catch (JsonProcessingException exception) {
            throw new ExternalServiceException(AI_INVALID_RESPONSE_MESSAGE);
        }
    }
}
