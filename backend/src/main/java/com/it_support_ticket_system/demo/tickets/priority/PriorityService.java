package com.it_support_ticket_system.demo.tickets.priority;

import com.it_support_ticket_system.demo.tickets.TicketPriority;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PriorityService {

    private static final List<String> HIGH_PRIORITY_KEYWORDS = List.of(
        "urgent",
        "critical",
        "system down",
        "server down",
        "production down",
        "cannot work",
        "blocked",
        "outage",
        "emergency",
        "immediately",
        "hitno",
        "ne mogu da radim",
        "sistem ne radi",
        "server ne radi"
    );

    private static final List<String> MEDIUM_PRIORITY_KEYWORDS = List.of(
        "login",
        "password",
        "account",
        "access",
        "authentication",
        "reset password",
        "cannot access",
        "nalog",
        "lozinka",
        "prijava",
        "pristup"
    );

    public TicketPriority determinePriority(String title, String description, String predictedCategory) {
        String normalizedText = ((title == null ? "" : title) + " " + (description == null ? "" : description))
            .toLowerCase(Locale.ROOT);

        if (containsAny(normalizedText, HIGH_PRIORITY_KEYWORDS)) {
            return TicketPriority.HIGH;
        }

        if (containsAny(normalizedText, MEDIUM_PRIORITY_KEYWORDS)) {
            return TicketPriority.MEDIUM;
        }

        String normalizedCategory = predictedCategory == null ? "" : predictedCategory.toLowerCase(Locale.ROOT);
        if (normalizedCategory.contains("access") || normalizedCategory.contains("login")) {
            return TicketPriority.MEDIUM;
        }

        return TicketPriority.LOW;
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
}
