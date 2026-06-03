package com.it_support_ticket_system.demo.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.it_support_ticket_system.demo.common.AiServiceUnavailableException;
import com.it_support_ticket_system.demo.common.ExternalServiceException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiPredictionIntegrationTest {

    private static final AtomicReference<String> RESPONSE_BODY = new AtomicReference<>();
    private static final AtomicReference<String> RESPONSE_CONTENT_TYPE = new AtomicReference<>();
    private static final AtomicLong RESPONSE_DELAY_MILLIS = new AtomicLong();
    private static volatile int responseStatus;
    private static HttpServer httpServer;

    @Autowired
    private AiPredictionService aiPredictionService;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        startServerIfNeeded();
        registry.add("app.ai.base-url", () -> "http://127.0.0.1:" + httpServer.getAddress().getPort());
        registry.add("app.ai.timeout-seconds", () -> 1);
        registry.add("app.ai.top-predictions-limit", () -> 2);
    }

    @BeforeEach
    void resetResponse() {
        responseStatus = 200;
        RESPONSE_DELAY_MILLIS.set(0);
        RESPONSE_CONTENT_TYPE.set(MediaType.APPLICATION_JSON_VALUE);
        RESPONSE_BODY.set("""
            {
              "predicted_category": "Hardware",
              "confidence": 0.82,
              "top_predictions": [
                {
                  "category": "Hardware",
                  "probability": 0.82
                },
                {
                  "category": "Software",
                  "probability": 0.11
                },
                {
                  "category": "Miscellaneous",
                  "probability": 0.07
                }
              ]
            }
            """);
    }

    @AfterAll
    static void stopServer() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void predictMapsSuccessfulSnakeCaseResponse() {
        AiPredictionResult result = aiPredictionService.predict("Printer is not working after update.");

        assertThat(result.predictedCategory()).isEqualTo("Hardware");
        assertThat(result.confidence()).isEqualTo(0.82);
        assertThat(result.failed()).isFalse();
        assertThat(result.errorMessage()).isNull();
        assertThat(result.topPredictions()).hasSize(2);
        assertThat(result.topPredictions().get(0).category()).isEqualTo("Hardware");
        assertThat(result.topPredictions().get(1).category()).isEqualTo("Software");
    }

    @Test
    void predictHandlesTimeout() {
        RESPONSE_DELAY_MILLIS.set(1500);

        assertThatThrownBy(() -> aiPredictionService.predict("Printer is not working after update."))
            .isInstanceOf(AiServiceUnavailableException.class)
            .hasMessage("AI prediction service is unavailable.");
    }

    @Test
    void predictHandlesAiServerError() {
        responseStatus = 500;
        RESPONSE_BODY.set("""
            {
              "message": "internal error"
            }
            """);

        assertThatThrownBy(() -> aiPredictionService.predict("Printer is not working after update."))
            .isInstanceOf(AiServiceUnavailableException.class)
            .hasMessage("AI prediction service is unavailable.");
    }

    @Test
    void predictHandlesAiClientError() {
        responseStatus = 400;
        RESPONSE_BODY.set("""
            {
              "message": "bad request"
            }
            """);

        assertThatThrownBy(() -> aiPredictionService.predict("Printer is not working after update."))
            .isInstanceOf(ExternalServiceException.class)
            .hasMessage("AI prediction service rejected the request.");
    }

    @Test
    void predictHandlesInvalidAiResponse() {
        RESPONSE_BODY.set("""
            {
              "confidence": 0.82
            }
            """);

        assertThatThrownBy(() -> aiPredictionService.predict("Printer is not working after update."))
            .isInstanceOf(ExternalServiceException.class)
            .hasMessage("AI prediction service returned an invalid response.");
    }

    @Test
    void predictEndpointReturnsPredictionWhenAiWorks() throws Exception {
        mockMvc.perform(post("/api/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "text": "Printer is not working after update."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.predictedCategory").value("Hardware"))
            .andExpect(jsonPath("$.confidence").value(0.82))
            .andExpect(jsonPath("$.topPredictions.length()").value(2))
            .andExpect(jsonPath("$.topPredictions[0].category").value("Hardware"))
            .andExpect(jsonPath("$.topPredictions[0].probability").value(0.82));
    }

    @Test
    void predictEndpointReturnsControlledErrorWhenAiFails() throws Exception {
        responseStatus = 500;
        RESPONSE_BODY.set("""
            {
              "message": "internal error"
            }
            """);

        mockMvc.perform(post("/api/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "text": "Printer is not working after update."
                    }
                    """))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.message").value("AI prediction service is unavailable."));
    }

    private static synchronized void startServerIfNeeded() {
        if (httpServer != null) {
            return;
        }

        try {
            httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start test AI server.", exception);
        }

        httpServer.createContext("/predict", exchange -> {
            try {
                long delay = RESPONSE_DELAY_MILLIS.get();
                if (delay > 0) {
                    Thread.sleep(delay);
                }

                byte[] responseBytes = RESPONSE_BODY.get().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", RESPONSE_CONTENT_TYPE.get());
                exchange.sendResponseHeaders(responseStatus, responseBytes.length);

                try (OutputStream outputStream = exchange.getResponseBody()) {
                    outputStream.write(responseBytes);
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
                // The client may time out and close the connection before the test server writes the response.
            } finally {
                exchange.close();
            }
        });
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();
    }
}
