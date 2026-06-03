package com.it_support_ticket_system.demo.ai;

import jakarta.validation.constraints.NotBlank;

public record AiPredictionRequest(
    @NotBlank(message = "Text is required.")
    String text
) {
}
