package com.it_support_ticket_system.demo.tickets;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(
    @Pattern(regexp = ".*\\S.*", message = "Final category must not be blank.")
    @Size(max = 100, message = "Final category must be at most 100 characters long.")
    String finalCategory,
    TicketPriority priority,
    TicketStatus status
) {
    public boolean isEmpty() {
        return finalCategory == null && priority == null && status == null;
    }
}
