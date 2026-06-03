package com.it_support_ticket_system.demo.statistics;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.it_support_ticket_system.demo.tickets.Ticket;
import com.it_support_ticket_system.demo.tickets.TicketPriority;
import com.it_support_ticket_system.demo.tickets.TicketRepository;
import com.it_support_ticket_system.demo.tickets.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatisticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void cleanUp() {
        ticketRepository.deleteAll();
    }

    @Test
    void emptyDatabaseReturnsZeroStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTickets").value(0))
            .andExpect(jsonPath("$.openTickets").value(0))
            .andExpect(jsonPath("$.closedTickets").value(0))
            .andExpect(jsonPath("$.averageConfidence").value(0.0))
            .andExpect(jsonPath("$.aiAcceptanceRate").value(0.0))
            .andExpect(jsonPath("$.aiFailedCount").value(0))
            .andExpect(jsonPath("$.ticketsByStatus.NEW").value(0))
            .andExpect(jsonPath("$.ticketsByStatus.IN_PROGRESS").value(0))
            .andExpect(jsonPath("$.ticketsByStatus.RESOLVED").value(0))
            .andExpect(jsonPath("$.ticketsByStatus.CLOSED").value(0))
            .andExpect(jsonPath("$.ticketsByPriority.LOW").value(0))
            .andExpect(jsonPath("$.ticketsByPriority.MEDIUM").value(0))
            .andExpect(jsonPath("$.ticketsByPriority.HIGH").value(0))
            .andExpect(jsonPath("$.ticketsByCategory").isMap())
            .andExpect(jsonPath("$.ticketsByCategory").isEmpty());
    }

    @Test
    void statisticsAreCalculatedFromTickets() throws Exception {
        createTicket("Access issue", "Access", "Access", 0.8, TicketPriority.MEDIUM, TicketStatus.NEW, true, false);
        createTicket("Hardware issue", "Hardware", "Hardware", 0.6, TicketPriority.HIGH, TicketStatus.IN_PROGRESS, true, false);
        createTicket(
            "Rights issue",
            "Hardware",
            "Administrative rights",
            0.4,
            TicketPriority.LOW,
            TicketStatus.RESOLVED,
            false,
            false
        );
        createTicket(
            "Fallback issue",
            "Miscellaneous",
            "Miscellaneous",
            0.0,
            TicketPriority.LOW,
            TicketStatus.CLOSED,
            false,
            true
        );

        mockMvc.perform(get("/api/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTickets").value(4))
            .andExpect(jsonPath("$.openTickets").value(2))
            .andExpect(jsonPath("$.closedTickets").value(2))
            .andExpect(jsonPath("$.averageConfidence").value(0.45))
            .andExpect(jsonPath("$.aiAcceptanceRate").value(0.5))
            .andExpect(jsonPath("$.aiFailedCount").value(1))
            .andExpect(jsonPath("$.ticketsByStatus.NEW").value(1))
            .andExpect(jsonPath("$.ticketsByStatus.IN_PROGRESS").value(1))
            .andExpect(jsonPath("$.ticketsByStatus.RESOLVED").value(1))
            .andExpect(jsonPath("$.ticketsByStatus.CLOSED").value(1))
            .andExpect(jsonPath("$.ticketsByPriority.LOW").value(2))
            .andExpect(jsonPath("$.ticketsByPriority.MEDIUM").value(1))
            .andExpect(jsonPath("$.ticketsByPriority.HIGH").value(1))
            .andExpect(jsonPath("$.ticketsByCategory.Access").value(1))
            .andExpect(jsonPath("$.ticketsByCategory.Hardware").value(1))
            .andExpect(jsonPath("$.ticketsByCategory['Administrative rights']").value(1))
            .andExpect(jsonPath("$.ticketsByCategory.Miscellaneous").value(1));
    }

    private void createTicket(
        String title,
        String predictedCategory,
        String finalCategory,
        double confidence,
        TicketPriority priority,
        TicketStatus status,
        boolean aiAccepted,
        boolean aiFailed
    ) {
        Ticket ticket = BeanUtils.instantiateClass(Ticket.class);
        ticket.setTitle(title);
        ticket.setDescription(title + " description.");
        ticket.setUserEmail(title.replace(' ', '.').toLowerCase() + "@example.com");
        ticket.setPredictedCategory(predictedCategory);
        ticket.setConfidence(confidence);
        ticket.setFinalCategory(finalCategory);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setAiAccepted(aiAccepted);
        ticket.setAiFailed(aiFailed);
        if (aiFailed) {
            ticket.setAiErrorMessage("AI prediction service is unavailable.");
        }
        ticketRepository.save(ticket);
    }
}
