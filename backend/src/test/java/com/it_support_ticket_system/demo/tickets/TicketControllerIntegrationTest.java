package com.it_support_ticket_system.demo.tickets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void cleanUp() {
        ticketRepository.deleteAll();
    }

    @Test
    void createTicketWithValidRequest() throws Exception {
        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Login problem",
                      "description": "I cannot access my account after password reset.",
                      "userEmail": "user@example.com",
                      "notes": "This started this morning."
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Login problem"))
            .andExpect(jsonPath("$.predictedCategory").value("Miscellaneous"))
            .andExpect(jsonPath("$.confidence").value(0.0))
            .andExpect(jsonPath("$.finalCategory").value("Miscellaneous"))
            .andExpect(jsonPath("$.priority").value("LOW"))
            .andExpect(jsonPath("$.status").value("NEW"))
            .andExpect(jsonPath("$.aiAccepted").value(true))
            .andExpect(jsonPath("$.aiFailed").value(false))
            .andExpect(jsonPath("$.topPredictions").isArray())
            .andExpect(jsonPath("$.topPredictions").isEmpty());
    }

    @Test
    void createTicketWithInvalidRequestReturns400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "",
                      "description": "bad",
                      "userEmail": "not-an-email"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void listTicketsReturnsCreatedTickets() throws Exception {
        createSampleTicket();

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Printer issue"))
            .andExpect(jsonPath("$.content[0].predictedCategory").value("Miscellaneous"))
            .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void getTicketByIdReturnsTicket() throws Exception {
        Long ticketId = createSampleTicket();

        mockMvc.perform(get("/api/tickets/{id}", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ticketId))
            .andExpect(jsonPath("$.title").value("Printer issue"))
            .andExpect(jsonPath("$.userEmail").value("printer@example.com"));
    }

    @Test
    void getMissingTicketReturns404() throws Exception {
        mockMvc.perform(get("/api/tickets/{id}", 9999))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Ticket with id 9999 was not found."));
    }

    @Test
    void updateTicketChangesAllowedFieldsOnly() throws Exception {
        Long ticketId = createSampleTicket();

        mockMvc.perform(put("/api/tickets/{id}", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "finalCategory": "Hardware",
                      "priority": "HIGH",
                      "status": "IN_PROGRESS"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ticketId))
            .andExpect(jsonPath("$.title").value("Printer issue"))
            .andExpect(jsonPath("$.predictedCategory").value("Miscellaneous"))
            .andExpect(jsonPath("$.confidence").value(0.0))
            .andExpect(jsonPath("$.finalCategory").value("Hardware"))
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.aiAccepted").value(false));
    }

    @Test
    void emptyUpdateReturns400() throws Exception {
        Long ticketId = createSampleTicket();

        mockMvc.perform(put("/api/tickets/{id}", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("At least one field must be provided for update."));
    }

    private Long createSampleTicket() {
        Ticket ticket = new Ticket();
        ticket.setTitle("Printer issue");
        ticket.setDescription("Office printer stopped working.");
        ticket.setUserEmail("printer@example.com");
        ticket.setPredictedCategory("Miscellaneous");
        ticket.setConfidence(0.0);
        ticket.setFinalCategory("Miscellaneous");
        ticket.setPriority(TicketPriority.LOW);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setAiAccepted(true);
        ticket.setAiFailed(false);
        return ticketRepository.save(ticket).getId();
    }
}
