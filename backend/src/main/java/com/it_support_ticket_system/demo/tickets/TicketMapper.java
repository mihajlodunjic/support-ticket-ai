package com.it_support_ticket_system.demo.tickets;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getUserEmail(),
            ticket.getNotes(),
            ticket.getPredictedCategory(),
            ticket.getConfidence(),
            ticket.getFinalCategory(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.isAiAccepted(),
            ticket.isAiFailed(),
            ticket.getAiErrorMessage(),
            toTopPredictions(ticket.getPredictions()),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }

    public TicketListItemResponse toListItemResponse(Ticket ticket) {
        return new TicketListItemResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getUserEmail(),
            ticket.getPredictedCategory(),
            ticket.getConfidence(),
            ticket.getFinalCategory(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.isAiAccepted(),
            ticket.isAiFailed(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }

    private List<TopPredictionResponse> toTopPredictions(List<TicketPrediction> predictions) {
        return predictions.stream()
            .map(prediction -> new TopPredictionResponse(
                prediction.getCategory(),
                prediction.getProbability(),
                prediction.getRank()
            ))
            .toList();
    }
}
