package com.it_support_ticket_system.demo.common;

import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import com.it_support_ticket_system.demo.config.AppProperties;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommonExceptionTestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validationErrorReturnsExpectedStructure() throws Exception {
        mockMvc.perform(post("/api/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed."))
            .andExpect(jsonPath("$.path").value("/api/test/validation"))
            .andExpect(jsonPath("$.validationErrors[0].field").value("name"));
    }

    @Test
    void notFoundExceptionReturns404() throws Exception {
        mockMvc.perform(get("/api/test/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Ticket with id 99 was not found."));
    }

    @Test
    void badRequestExceptionReturns400() throws Exception {
        mockMvc.perform(get("/api/test/bad-request"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Request payload is invalid."));
    }

    @Test
    void conflictExceptionReturns409() throws Exception {
        mockMvc.perform(get("/api/test/conflict"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("Category already exists."));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        AppProperties appProperties() {
            return new AppProperties();
        }
    }
}
