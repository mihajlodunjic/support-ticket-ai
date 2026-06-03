package com.it_support_ticket_system.demo.tickets;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;

public class TicketFilterRequest {

    private TicketStatus status;
    private TicketPriority priority;
    private String predictedCategory;
    private String finalCategory;
    private String userEmail;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant createdTo;

    @DecimalMin(value = "0.0", message = "Minimum confidence must be between 0 and 1.")
    @DecimalMax(value = "1.0", message = "Minimum confidence must be between 0 and 1.")
    private Double minConfidence;

    @DecimalMin(value = "0.0", message = "Maximum confidence must be between 0 and 1.")
    @DecimalMax(value = "1.0", message = "Maximum confidence must be between 0 and 1.")
    private Double maxConfidence;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getPredictedCategory() {
        return predictedCategory;
    }

    public void setPredictedCategory(String predictedCategory) {
        this.predictedCategory = predictedCategory;
    }

    public String getFinalCategory() {
        return finalCategory;
    }

    public void setFinalCategory(String finalCategory) {
        this.finalCategory = finalCategory;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Instant getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(Instant createdFrom) {
        this.createdFrom = createdFrom;
    }

    public Instant getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(Instant createdTo) {
        this.createdTo = createdTo;
    }

    public Double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(Double minConfidence) {
        this.minConfidence = minConfidence;
    }

    public Double getMaxConfidence() {
        return maxConfidence;
    }

    public void setMaxConfidence(Double maxConfidence) {
        this.maxConfidence = maxConfidence;
    }

    @AssertTrue(message = "createdFrom must be before or equal to createdTo.")
    public boolean isCreatedRangeValid() {
        return createdFrom == null || createdTo == null || !createdFrom.isAfter(createdTo);
    }

    @AssertTrue(message = "minConfidence must be less than or equal to maxConfidence.")
    public boolean isConfidenceRangeValid() {
        return minConfidence == null || maxConfidence == null || minConfidence <= maxConfidence;
    }
}
