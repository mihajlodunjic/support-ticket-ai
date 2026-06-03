package com.it_support_ticket_system.demo.tickets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void ticketCanBeSavedAndLoadedWithPredictions() {
        Ticket ticket = new Ticket();
        ticket.setTitle("Login problem");
        ticket.setDescription("I cannot access my account after password reset.");
        ticket.setUserEmail("user@example.com");
        ticket.setNotes("Created from repository test.");
        ticket.setPredictedCategory("Access");
        ticket.setConfidence(0.87);
        ticket.setFinalCategory("Access");
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setAiAccepted(true);
        ticket.setAiFailed(false);
        ticket.addPrediction(new TicketPrediction("Access", 0.87, 1));
        ticket.addPrediction(new TicketPrediction("Hardware", 0.03, 2));

        Ticket savedTicket = ticketRepository.saveAndFlush(ticket);
        Optional<Ticket> reloadedTicket = ticketRepository.findById(savedTicket.getId());

        assertThat(reloadedTicket).isPresent();
        assertThat(reloadedTicket.get().getCreatedAt()).isNotNull();
        assertThat(reloadedTicket.get().getUpdatedAt()).isNotNull();
        assertThat(reloadedTicket.get().getStatus()).isEqualTo(TicketStatus.NEW);
        assertThat(reloadedTicket.get().getPriority()).isEqualTo(TicketPriority.MEDIUM);
        assertThat(reloadedTicket.get().getPredictions()).hasSize(2);
        assertThat(reloadedTicket.get().getPredictions())
            .extracting(TicketPrediction::getCategory)
            .containsExactly("Access", "Hardware");
    }

    @Test
    void ticketEnumsArePersistedAsStrings() {
        Ticket ticket = new Ticket();
        ticket.setTitle("System outage");
        ticket.setDescription("Production is down.");
        ticket.setUserEmail("admin@example.com");
        ticket.setPredictedCategory("Miscellaneous");
        ticket.setConfidence(0.42);
        ticket.setFinalCategory("Miscellaneous");
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAiAccepted(false);
        ticket.setAiFailed(true);
        ticket.setAiErrorMessage("AI service unavailable.");

        Ticket savedTicket = ticketRepository.saveAndFlush(ticket);

        assertThat(savedTicket.getId()).isNotNull();
        assertThat(ticketRepository.findById(savedTicket.getId()))
            .get()
            .satisfies(loadedTicket -> {
                assertThat(loadedTicket.getPriority()).isEqualTo(TicketPriority.HIGH);
                assertThat(loadedTicket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
                assertThat(loadedTicket.isAiFailed()).isTrue();
            });
    }
}
