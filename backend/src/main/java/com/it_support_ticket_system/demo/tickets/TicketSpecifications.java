package com.it_support_ticket_system.demo.tickets;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> withFilters(TicketFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getPriority() != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), filter.getPriority()));
            }

            if (hasText(filter.getPredictedCategory())) {
                predicates.add(
                    criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("predictedCategory")),
                        filter.getPredictedCategory().trim().toLowerCase(Locale.ROOT)
                    )
                );
            }

            if (hasText(filter.getFinalCategory())) {
                predicates.add(
                    criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("finalCategory")),
                        filter.getFinalCategory().trim().toLowerCase(Locale.ROOT)
                    )
                );
            }

            if (hasText(filter.getUserEmail())) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("userEmail")),
                        "%" + filter.getUserEmail().trim().toLowerCase(Locale.ROOT) + "%"
                    )
                );
            }

            if (filter.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
            }

            if (filter.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
            }

            if (filter.getMinConfidence() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("confidence"), filter.getMinConfidence()));
            }

            if (filter.getMaxConfidence() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("confidence"), filter.getMaxConfidence()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
