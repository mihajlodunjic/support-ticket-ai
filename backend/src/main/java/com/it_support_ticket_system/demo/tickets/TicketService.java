package com.it_support_ticket_system.demo.tickets;

import com.it_support_ticket_system.demo.categories.Category;
import com.it_support_ticket_system.demo.categories.CategoryRepository;
import com.it_support_ticket_system.demo.common.BadRequestException;
import com.it_support_ticket_system.demo.common.PageResponse;
import com.it_support_ticket_system.demo.common.ResourceNotFoundException;
import com.it_support_ticket_system.demo.config.AppProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final AppProperties appProperties;
    private final TicketMapper ticketMapper;

    public TicketService(
        TicketRepository ticketRepository,
        CategoryRepository categoryRepository,
        AppProperties appProperties,
        TicketMapper ticketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.appProperties = appProperties;
        this.ticketMapper = ticketMapper;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        Category placeholderCategory = getCategoryOrThrow(appProperties.getAi().getFallbackCategory());

        Ticket ticket = new Ticket();
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setUserEmail(request.userEmail().trim());
        ticket.setNotes(trimToNull(request.notes()));
        ticket.setPredictedCategory(placeholderCategory.getName());
        ticket.setConfidence(0.0);
        ticket.setFinalCategory(placeholderCategory.getName());
        ticket.setPriority(TicketPriority.LOW);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setAiAccepted(true);
        ticket.setAiFailed(false);
        ticket.setAiErrorMessage(null);

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketListItemResponse> getTickets(Pageable pageable) {
        Page<TicketListItemResponse> page = ticketRepository.findAll(pageable)
            .map(ticketMapper::toListItemResponse);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket with id %d was not found.".formatted(id)));

        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        if (request.isEmpty()) {
            throw new BadRequestException("At least one field must be provided for update.");
        }

        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket with id %d was not found.".formatted(id)));

        if (request.finalCategory() != null) {
            Category category = getCategoryOrThrow(request.finalCategory());
            ticket.setFinalCategory(category.getName());
        }

        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }

        if (request.status() != null) {
            ticket.setStatus(request.status());
        }

        ticket.setAiAccepted(ticket.getFinalCategory().equalsIgnoreCase(ticket.getPredictedCategory()));

        Ticket updatedTicket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updatedTicket);
    }

    private Category getCategoryOrThrow(String categoryName) {
        return categoryRepository.findByNameIgnoreCase(categoryName.trim())
            .orElseThrow(() -> new BadRequestException("Category '%s' does not exist.".formatted(categoryName)));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
