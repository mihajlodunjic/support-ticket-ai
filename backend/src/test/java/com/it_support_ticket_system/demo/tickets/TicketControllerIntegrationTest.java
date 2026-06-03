package com.it_support_ticket_system.demo.tickets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.it_support_ticket_system.demo.ai.AiPredictionResult;
import com.it_support_ticket_system.demo.ai.AiPredictionService;
import com.it_support_ticket_system.demo.common.AiServiceUnavailableException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketPredictionRepository ticketPredictionRepository;

    @MockBean
    private AiPredictionService aiPredictionService;

    @BeforeEach
    void cleanUp() {
        ticketRepository.deleteAll();
        reset(aiPredictionService);
    }

    @Test
    void createTicketWithValidRequest() throws Exception {
        when(aiPredictionService.predict(anyString())).thenReturn(AiPredictionResult.success(
            "Access",
            0.91,
            List.of(
                new AiPredictionResult.TopPrediction("Access", 0.91),
                new AiPredictionResult.TopPrediction("Administrative rights", 0.07)
            )
        ));

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
            .andExpect(jsonPath("$.predictedCategory").value("Access"))
            .andExpect(jsonPath("$.confidence").value(0.91))
            .andExpect(jsonPath("$.finalCategory").value("Access"))
            .andExpect(jsonPath("$.priority").value("MEDIUM"))
            .andExpect(jsonPath("$.status").value("NEW"))
            .andExpect(jsonPath("$.aiAccepted").value(true))
            .andExpect(jsonPath("$.aiFailed").value(false))
            .andExpect(jsonPath("$.topPredictions").isArray())
            .andExpect(jsonPath("$.topPredictions.length()").value(2))
            .andExpect(jsonPath("$.topPredictions[0].category").value("Access"))
            .andExpect(jsonPath("$.topPredictions[0].probability").value(0.91))
            .andExpect(jsonPath("$.topPredictions[0].rank").value(1))
            .andExpect(jsonPath("$.topPredictions[1].category").value("Administrative rights"))
            .andExpect(jsonPath("$.topPredictions[1].rank").value(2));

        Ticket storedTicket = ticketRepository.findAll().getFirst();
        verify(aiPredictionService).predict("Login problem" + System.lineSeparator() + "I cannot access my account after password reset.");
        org.assertj.core.api.Assertions.assertThat(storedTicket.getPredictedCategory()).isEqualTo("Access");
        org.assertj.core.api.Assertions.assertThat(storedTicket.getPriority()).isEqualTo(TicketPriority.MEDIUM);
        org.assertj.core.api.Assertions.assertThat(ticketPredictionRepository.findByTicketIdOrderByRankAsc(storedTicket.getId()))
            .hasSize(2);
    }

    @Test
    void createTicketFallsBackWhenAiIsUnavailable() throws Exception {
        when(aiPredictionService.predict(anyString())).thenThrow(
            new AiServiceUnavailableException("AI prediction service is unavailable.")
        );

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Printer setup",
                      "description": "I need help adding a new printer to the office.",
                      "userEmail": "user@example.com"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.predictedCategory").value("Miscellaneous"))
            .andExpect(jsonPath("$.confidence").value(0.0))
            .andExpect(jsonPath("$.finalCategory").value("Miscellaneous"))
            .andExpect(jsonPath("$.priority").value("LOW"))
            .andExpect(jsonPath("$.status").value("NEW"))
            .andExpect(jsonPath("$.aiAccepted").value(false))
            .andExpect(jsonPath("$.aiFailed").value(true))
            .andExpect(jsonPath("$.aiErrorMessage").value("AI prediction service is unavailable."))
            .andExpect(jsonPath("$.topPredictions").isArray())
            .andExpect(jsonPath("$.topPredictions").isEmpty());

        Ticket storedTicket = ticketRepository.findAll().getFirst();
        org.assertj.core.api.Assertions.assertThat(storedTicket.isAiFailed()).isTrue();
        org.assertj.core.api.Assertions.assertThat(storedTicket.getPredictedCategory()).isEqualTo("Miscellaneous");
        org.assertj.core.api.Assertions.assertThat(ticketPredictionRepository.findByTicketIdOrderByRankAsc(storedTicket.getId()))
            .isEmpty();
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

        mockMvc.perform(get("/api/admin/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Printer issue"))
            .andExpect(jsonPath("$.content[0].predictedCategory").value("Miscellaneous"))
            .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void listTicketsSupportsPaginationAndDefaultSortByNewestFirst() throws Exception {
        Long olderTicketId = createSampleTicket();
        sleepForTimestampOrdering();
        Long newerTicketId = createCustomTicket(
            "Newest issue",
            "Latest created ticket.",
            "newest@example.com",
            "Hardware",
            "Hardware",
            0.90,
            TicketPriority.HIGH,
            TicketStatus.IN_PROGRESS,
            true,
            false
        );

        mockMvc.perform(get("/api/admin/tickets").param("page", "0").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(newerTicketId))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false));

        org.assertj.core.api.Assertions.assertThat(newerTicketId).isGreaterThan(olderTicketId);
    }

    @Test
    void filterByStatusReturnsMatchingTickets() throws Exception {
        createCustomTicket(
            "New issue",
            "Should stay new.",
            "new@example.com",
            "Hardware",
            "Hardware",
            0.80,
            TicketPriority.LOW,
            TicketStatus.NEW,
            true,
            false
        );
        createCustomTicket(
            "Resolved issue",
            "Already solved.",
            "resolved@example.com",
            "Access",
            "Access",
            0.92,
            TicketPriority.MEDIUM,
            TicketStatus.RESOLVED,
            true,
            false
        );

        mockMvc.perform(get("/api/admin/tickets").param("status", "RESOLVED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].status").value("RESOLVED"))
            .andExpect(jsonPath("$.content[0].title").value("Resolved issue"));
    }

    @Test
    void filterByPriorityReturnsMatchingTickets() throws Exception {
        createCustomTicket(
            "Low issue",
            "Low priority item.",
            "low@example.com",
            "Hardware",
            "Hardware",
            0.35,
            TicketPriority.LOW,
            TicketStatus.NEW,
            true,
            false
        );
        createCustomTicket(
            "High issue",
            "High priority item.",
            "high@example.com",
            "Access",
            "Access",
            0.95,
            TicketPriority.HIGH,
            TicketStatus.IN_PROGRESS,
            true,
            false
        );

        mockMvc.perform(get("/api/admin/tickets").param("priority", "HIGH"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
            .andExpect(jsonPath("$.content[0].title").value("High issue"));
    }

    @Test
    void filterByPredictedCategoryReturnsMatchingTickets() throws Exception {
        createCustomTicket(
            "Hardware issue",
            "Hardware related ticket.",
            "hardware@example.com",
            "Hardware",
            "Hardware",
            0.70,
            TicketPriority.LOW,
            TicketStatus.NEW,
            true,
            false
        );
        createCustomTicket(
            "Access issue",
            "Access related ticket.",
            "access@example.com",
            "Access",
            "Access",
            0.91,
            TicketPriority.MEDIUM,
            TicketStatus.NEW,
            true,
            false
        );

        mockMvc.perform(get("/api/admin/tickets").param("predictedCategory", "access"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].predictedCategory").value("Access"))
            .andExpect(jsonPath("$.content[0].title").value("Access issue"));
    }

    @Test
    void filterByFinalCategoryReturnsMatchingTickets() throws Exception {
        createCustomTicket(
            "Hardware issue",
            "Hardware related ticket.",
            "hardware@example.com",
            "Hardware",
            "Administrative rights",
            0.70,
            TicketPriority.LOW,
            TicketStatus.NEW,
            false,
            false
        );
        createCustomTicket(
            "Access issue",
            "Access related ticket.",
            "access@example.com",
            "Access",
            "Access",
            0.91,
            TicketPriority.MEDIUM,
            TicketStatus.NEW,
            true,
            false
        );

        mockMvc.perform(get("/api/admin/tickets").param("finalCategory", "administrative rights"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].finalCategory").value("Administrative rights"))
            .andExpect(jsonPath("$.content[0].title").value("Hardware issue"));
    }

    @Test
    void filterByUserEmailReturnsMatchingTickets() throws Exception {
        createCustomTicket(
            "Hardware issue",
            "Hardware related ticket.",
            "hardware.team@example.com",
            "Hardware",
            "Hardware",
            0.70,
            TicketPriority.LOW,
            TicketStatus.NEW,
            true,
            false
        );
        createCustomTicket(
            "Access issue",
            "Access related ticket.",
            "access.team@example.com",
            "Access",
            "Access",
            0.91,
            TicketPriority.MEDIUM,
            TicketStatus.NEW,
            true,
            false
        );

        mockMvc.perform(get("/api/admin/tickets").param("userEmail", "access.team"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].userEmail").value("access.team@example.com"))
            .andExpect(jsonPath("$.content[0].title").value("Access issue"));
    }

    @Test
    void filterByConfidenceRangeReturnsMatchingTickets() throws Exception {
        createCustomTicket(
            "Low confidence",
            "Low confidence ticket.",
            "low@example.com",
            "Hardware",
            "Hardware",
            0.25,
            TicketPriority.LOW,
            TicketStatus.NEW,
            true,
            false
        );
        createCustomTicket(
            "High confidence",
            "High confidence ticket.",
            "high@example.com",
            "Access",
            "Access",
            0.91,
            TicketPriority.MEDIUM,
            TicketStatus.NEW,
            true,
            false
        );

        mockMvc.perform(get("/api/admin/tickets").param("minConfidence", "0.8").param("maxConfidence", "1.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].confidence").value(0.91))
            .andExpect(jsonPath("$.content[0].title").value("High confidence"));
    }

    @Test
    void filterByCreatedDateRangeReturnsMatchingTickets() throws Exception {
        Long olderTicketId = createCustomTicket(
            "Older issue",
            "Older created ticket.",
            "older@example.com",
            "Hardware",
            "Hardware",
            0.40,
            TicketPriority.LOW,
            TicketStatus.NEW,
            true,
            false
        );
        Instant olderCreatedAt = ticketRepository.findById(olderTicketId).orElseThrow().getCreatedAt();
        sleepForTimestampOrdering();
        Long newerTicketId = createCustomTicket(
            "Newer issue",
            "Newer created ticket.",
            "newer@example.com",
            "Access",
            "Access",
            0.90,
            TicketPriority.MEDIUM,
            TicketStatus.NEW,
            true,
            false
        );
        Instant newerCreatedAt = ticketRepository.findById(newerTicketId).orElseThrow().getCreatedAt();

        mockMvc.perform(get("/api/admin/tickets")
                .param("createdFrom", newerCreatedAt.minusMillis(1).toString())
                .param("createdTo", newerCreatedAt.plusMillis(1).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(newerTicketId))
            .andExpect(jsonPath("$.content[0].title").value("Newer issue"));

        org.assertj.core.api.Assertions.assertThat(newerCreatedAt).isAfter(olderCreatedAt);
    }

    @Test
    void getTicketByIdReturnsTicket() throws Exception {
        Long ticketId = createSampleTicket();

        mockMvc.perform(get("/api/admin/tickets/{id}", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ticketId))
            .andExpect(jsonPath("$.title").value("Printer issue"))
            .andExpect(jsonPath("$.userEmail").value("printer@example.com"));
    }

    @Test
    void getMissingTicketReturns404() throws Exception {
        mockMvc.perform(get("/api/admin/tickets/{id}", 9999))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Ticket with id 9999 was not found."));
    }

    @Test
    void updateTicketChangesAllowedFieldsOnly() throws Exception {
        Long ticketId = createSampleTicket();
        Ticket original = ticketRepository.findById(ticketId).orElseThrow();
        String originalPredictedCategory = original.getPredictedCategory();
        double originalConfidence = original.getConfidence();

        mockMvc.perform(put("/api/admin/tickets/{id}", ticketId)
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

        Ticket updated = ticketRepository.findById(ticketId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updated.getPredictedCategory()).isEqualTo(originalPredictedCategory);
        org.assertj.core.api.Assertions.assertThat(updated.getConfidence()).isEqualTo(originalConfidence);
    }

    @Test
    void updatingFinalCategoryToPredictedCategoryKeepsAiAcceptedTrue() throws Exception {
        Long ticketId = createSampleTicket();

        mockMvc.perform(put("/api/admin/tickets/{id}", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "finalCategory": "Miscellaneous"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.finalCategory").value("Miscellaneous"))
            .andExpect(jsonPath("$.aiAccepted").value(true));
    }

    @Test
    void updatingStatusOnlyDoesNotChangePredictedCategory() throws Exception {
        Long ticketId = createSampleTicket();
        Ticket beforeUpdate = ticketRepository.findById(ticketId).orElseThrow();

        mockMvc.perform(put("/api/admin/tickets/{id}", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "RESOLVED"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RESOLVED"))
            .andExpect(jsonPath("$.predictedCategory").value("Miscellaneous"));

        Ticket afterUpdate = ticketRepository.findById(ticketId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(afterUpdate.getPredictedCategory()).isEqualTo(beforeUpdate.getPredictedCategory());
    }

    @Test
    void updatingPriorityOnlyDoesNotChangeConfidence() throws Exception {
        Long ticketId = createSampleTicket();
        Ticket beforeUpdate = ticketRepository.findById(ticketId).orElseThrow();

        mockMvc.perform(put("/api/admin/tickets/{id}", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "priority": "HIGH"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andExpect(jsonPath("$.confidence").value(0.0));

        Ticket afterUpdate = ticketRepository.findById(ticketId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(afterUpdate.getConfidence()).isEqualTo(beforeUpdate.getConfidence());
    }

    @Test
    void emptyUpdateReturns400() throws Exception {
        Long ticketId = createSampleTicket();

        mockMvc.perform(put("/api/admin/tickets/{id}", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("At least one field must be provided for update."));
    }

    private Long createSampleTicket() {
        return createCustomTicket(
            "Printer issue",
            "Office printer stopped working.",
            "printer@example.com",
            "Miscellaneous",
            "Miscellaneous",
            0.0,
            TicketPriority.LOW,
            TicketStatus.NEW,
            true,
            false
        );
    }

    private Long createCustomTicket(
        String title,
        String description,
        String userEmail,
        String predictedCategory,
        String finalCategory,
        double confidence,
        TicketPriority priority,
        TicketStatus status,
        boolean aiAccepted,
        boolean aiFailed
    ) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setUserEmail(userEmail);
        ticket.setPredictedCategory(predictedCategory);
        ticket.setConfidence(confidence);
        ticket.setFinalCategory(finalCategory);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setAiAccepted(aiAccepted);
        ticket.setAiFailed(aiFailed);
        return ticketRepository.save(ticket).getId();
    }

    private void sleepForTimestampOrdering() throws InterruptedException {
        Thread.sleep(15);
    }
}
