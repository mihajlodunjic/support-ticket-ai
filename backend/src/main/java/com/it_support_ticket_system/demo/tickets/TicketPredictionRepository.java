package com.it_support_ticket_system.demo.tickets;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketPredictionRepository extends JpaRepository<TicketPrediction, Long> {
}
