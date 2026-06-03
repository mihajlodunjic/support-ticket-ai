package com.it_support_ticket_system.demo.tickets;

import com.it_support_ticket_system.demo.ai.AiPredictionResult;
import com.it_support_ticket_system.demo.ai.AiPredictionService;
import com.it_support_ticket_system.demo.categories.Category;
import com.it_support_ticket_system.demo.categories.CategoryRepository;
import com.it_support_ticket_system.demo.common.AiServiceUnavailableException;
import com.it_support_ticket_system.demo.common.BadRequestException;
import com.it_support_ticket_system.demo.common.ExternalServiceException;
import com.it_support_ticket_system.demo.common.PageResponse;
import com.it_support_ticket_system.demo.common.ResourceNotFoundException;
import com.it_support_ticket_system.demo.config.AppProperties;
import com.it_support_ticket_system.demo.tickets.priority.PriorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final AppProperties appProperties;
    private final TicketMapper ticketMapper;
    private final AiPredictionService aiPredictionService;
    private final PriorityService priorityService;

    public TicketService(
        TicketRepository ticketRepository,
        CategoryRepository categoryRepository,
        AppProperties appProperties,
        TicketMapper ticketMapper,
        AiPredictionService aiPredictionService,
        PriorityService priorityService
    ) {
        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.appProperties = appProperties;
        this.ticketMapper = ticketMapper;
        this.aiPredictionService = aiPredictionService;
        this.priorityService = priorityService;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        String title = request.title().trim();
        String description = request.description().trim();
        String userEmail = request.userEmail().trim();
        String notes = trimToNull(request.notes());
        AiPredictionResult prediction = predictWithFallback(title, description);
        String predictedCategory = resolveAiCategoryName(prediction.predictedCategory());

        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setUserEmail(userEmail);
        ticket.setNotes(notes);
        ticket.setPredictedCategory(predictedCategory);
        ticket.setConfidence(prediction.confidence());
        ticket.setFinalCategory(predictedCategory);
        ticket.setPriority(priorityService.determinePriority(title, description, predictedCategory));
        ticket.setStatus(TicketStatus.NEW);
        ticket.setAiAccepted(!prediction.failed());
        ticket.setAiFailed(prediction.failed());
        ticket.setAiErrorMessage(prediction.errorMessage());
        addTopPredictions(ticket, prediction);

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketListItemResponse> getTickets(TicketFilterRequest filter, Pageable pageable) {
        Page<TicketListItemResponse> page = ticketRepository.findAll(TicketSpecifications.withFilters(filter), pageable)
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

    private String resolveAiCategoryName(String categoryName) {
        return categoryRepository.findByNameIgnoreCase(categoryName.trim())
            .map(Category::getName)
            .orElseThrow(() -> new ExternalServiceException("AI prediction service returned an invalid response."));
    }

    private AiPredictionResult predictWithFallback(String title, String description) {
        try {
            return aiPredictionService.predict(buildAiText(title, description));
        } catch (AiServiceUnavailableException exception) {
            String fallbackCategory = getCategoryOrThrow(appProperties.getAi().getFallbackCategory()).getName();
            log.warn("AI prediction unavailable, using fallback category '{}'.", fallbackCategory);
            return AiPredictionResult.failure(fallbackCategory, exception.getMessage());
        }
    }

    private String buildAiText(String title, String description) {
        return title + System.lineSeparator() + description;
    }

    private void addTopPredictions(Ticket ticket, AiPredictionResult prediction) {
        int rank = 1;
        for (AiPredictionResult.TopPrediction topPrediction : prediction.topPredictions()) {
            String categoryName = resolveAiCategoryName(topPrediction.category());
            ticket.addPrediction(new TicketPrediction(categoryName, topPrediction.probability(), rank));
            rank++;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
