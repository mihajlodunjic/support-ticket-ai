package com.it_support_ticket_system.demo.tickets;

import com.it_support_ticket_system.demo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "predicted_category", nullable = false, length = 100)
    private String predictedCategory;

    @Column(nullable = false)
    private double confidence;

    @Column(name = "final_category", nullable = false, length = 100)
    private String finalCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "ai_accepted", nullable = false)
    private boolean aiAccepted;

    @Column(name = "ai_failed", nullable = false)
    private boolean aiFailed;

    @Column(name = "ai_error_message", columnDefinition = "TEXT")
    private String aiErrorMessage;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rank ASC")
    private List<TicketPrediction> predictions = new ArrayList<>();

    protected Ticket() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPredictedCategory() {
        return predictedCategory;
    }

    public void setPredictedCategory(String predictedCategory) {
        this.predictedCategory = predictedCategory;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getFinalCategory() {
        return finalCategory;
    }

    public void setFinalCategory(String finalCategory) {
        this.finalCategory = finalCategory;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public boolean isAiAccepted() {
        return aiAccepted;
    }

    public void setAiAccepted(boolean aiAccepted) {
        this.aiAccepted = aiAccepted;
    }

    public boolean isAiFailed() {
        return aiFailed;
    }

    public void setAiFailed(boolean aiFailed) {
        this.aiFailed = aiFailed;
    }

    public String getAiErrorMessage() {
        return aiErrorMessage;
    }

    public void setAiErrorMessage(String aiErrorMessage) {
        this.aiErrorMessage = aiErrorMessage;
    }

    public List<TicketPrediction> getPredictions() {
        return predictions;
    }

    public void addPrediction(TicketPrediction prediction) {
        predictions.add(prediction);
        prediction.setTicket(this);
    }

    public void removePrediction(TicketPrediction prediction) {
        predictions.remove(prediction);
        prediction.setTicket(null);
    }
}
