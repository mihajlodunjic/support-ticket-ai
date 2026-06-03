package com.it_support_ticket_system.demo.statistics;

import java.util.Map;

public record StatisticsResponse(
    long totalTickets,
    long openTickets,
    long closedTickets,
    double averageConfidence,
    double aiAcceptanceRate,
    long aiFailedCount,
    Map<String, Long> ticketsByStatus,
    Map<String, Long> ticketsByPriority,
    Map<String, Long> ticketsByCategory
) {
}
