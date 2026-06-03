package com.it_support_ticket_system.demo.tickets.priority;

import static org.assertj.core.api.Assertions.assertThat;

import com.it_support_ticket_system.demo.tickets.TicketPriority;
import org.junit.jupiter.api.Test;

class PriorityServiceTest {

    private final PriorityService priorityService = new PriorityService();

    @Test
    void highKeywordReturnsHigh() {
        TicketPriority priority = priorityService.determinePriority(
            "Urgent production issue",
            "The system is down for everyone.",
            "Hardware"
        );

        assertThat(priority).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void mediumKeywordReturnsMedium() {
        TicketPriority priority = priorityService.determinePriority(
            "Login problem",
            "Password reset is not working.",
            "Hardware"
        );

        assertThat(priority).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void accessCategoryReturnsMedium() {
        TicketPriority priority = priorityService.determinePriority(
            "Question",
            "Please review this request when possible.",
            "Access"
        );

        assertThat(priority).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void neutralTextReturnsLow() {
        TicketPriority priority = priorityService.determinePriority(
            "Printer setup",
            "I need help adding a new printer to the office.",
            "Hardware"
        );

        assertThat(priority).isEqualTo(TicketPriority.LOW);
    }

    @Test
    void highPriorityWinsOverMedium() {
        TicketPriority priority = priorityService.determinePriority(
            "Urgent login issue",
            "Users cannot access the system and production is down.",
            "Access"
        );

        assertThat(priority).isEqualTo(TicketPriority.HIGH);
    }
}
