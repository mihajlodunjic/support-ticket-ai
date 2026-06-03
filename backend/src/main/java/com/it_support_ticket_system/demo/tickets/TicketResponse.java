package com.it_support_ticket_system.demo.tickets;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
    Long id,
    String title,
    String description,
    String userEmail,
    String notes,
    String predictedCategory,
    double confidence,
    String finalCategory,
    TicketPriority priority,
    TicketStatus status,
    boolean aiAccepted,
    boolean aiFailed,
    String aiErrorMessage,
    List<TopPredictionResponse> topPredictions,
    Instant createdAt,
    Instant updatedAt
) {
}
