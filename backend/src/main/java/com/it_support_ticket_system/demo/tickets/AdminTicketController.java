package com.it_support_ticket_system.demo.tickets;

import com.it_support_ticket_system.demo.common.PageResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tickets")
@SecurityRequirement(name = "bearerAuth")
public class AdminTicketController {

    private final TicketService ticketService;

    public AdminTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public PageResponse<TicketListItemResponse> getTickets(
        @Valid TicketFilterRequest filter,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ticketService.getTickets(filter, pageable);
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {
        return ticketService.getTicket(id);
    }

    @PutMapping("/{id}")
    public TicketResponse updateTicket(@PathVariable Long id, @Valid @RequestBody UpdateTicketRequest request) {
        return ticketService.updateTicket(id, request);
    }
}
