package com.it_support_ticket_system.demo.tickets;

import java.time.Instant;

public record TicketListItemResponse(
    Long id,
    String title,
    String userEmail,
    String predictedCategory,
    double confidence,
    String finalCategory,
    TicketPriority priority,
    TicketStatus status,
    boolean aiAccepted,
    boolean aiFailed,
    Instant createdAt,
    Instant updatedAt
) {
}
