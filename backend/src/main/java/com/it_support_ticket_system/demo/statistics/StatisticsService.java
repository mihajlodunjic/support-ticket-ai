package com.it_support_ticket_system.demo.statistics;

import com.it_support_ticket_system.demo.tickets.Ticket;
import com.it_support_ticket_system.demo.tickets.TicketPriority;
import com.it_support_ticket_system.demo.tickets.TicketRepository;
import com.it_support_ticket_system.demo.tickets.TicketStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticsService {

    private final TicketRepository ticketRepository;

    public StatisticsService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {
        List<Ticket> tickets = ticketRepository.findAll();

        long totalTickets = tickets.size();
        long openTickets = tickets.stream()
            .filter(ticket -> ticket.getStatus() == TicketStatus.NEW || ticket.getStatus() == TicketStatus.IN_PROGRESS)
            .count();
        long closedTickets = tickets.stream()
            .filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED)
            .count();
        double averageConfidence = tickets.stream()
            .mapToDouble(Ticket::getConfidence)
            .average()
            .orElse(0.0);
        long acceptedTickets = tickets.stream()
            .filter(Ticket::isAiAccepted)
            .count();
        double aiAcceptanceRate = totalTickets == 0 ? 0.0 : (double) acceptedTickets / totalTickets;
        long aiFailedCount = tickets.stream()
            .filter(Ticket::isAiFailed)
            .count();

        return new StatisticsResponse(
            totalTickets,
            openTickets,
            closedTickets,
            averageConfidence,
            aiAcceptanceRate,
            aiFailedCount,
            countByEnum(tickets, Ticket::getStatus, TicketStatus.values()),
            countByEnum(tickets, Ticket::getPriority, TicketPriority.values()),
            countByCategory(tickets)
        );
    }

    private <E extends Enum<E>> Map<String, Long> countByEnum(
        List<Ticket> tickets,
        Function<Ticket, E> classifier,
        E[] values
    ) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (E value : values) {
            counts.put(value.name(), 0L);
        }

        tickets.stream()
            .collect(Collectors.groupingBy(ticket -> classifier.apply(ticket).name(), Collectors.counting()))
            .forEach(counts::put);

        return counts;
    }

    private Map<String, Long> countByCategory(List<Ticket> tickets) {
        return tickets.stream()
            .collect(Collectors.groupingBy(Ticket::getFinalCategory, TreeMap::new, Collectors.counting()));
    }
}
