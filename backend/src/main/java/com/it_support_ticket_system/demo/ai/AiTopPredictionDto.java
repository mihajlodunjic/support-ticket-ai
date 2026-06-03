package com.it_support_ticket_system.demo.ai;

public record AiTopPredictionDto(
    String category,
    Double probability
) {
}
