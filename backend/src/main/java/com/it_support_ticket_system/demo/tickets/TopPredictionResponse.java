package com.it_support_ticket_system.demo.tickets;

public record TopPredictionResponse(
    String category,
    double probability,
    int rank
) {
}
