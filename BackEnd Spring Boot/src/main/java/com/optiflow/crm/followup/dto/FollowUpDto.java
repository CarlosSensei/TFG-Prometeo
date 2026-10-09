package com.optiflow.crm.followup.dto;

import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class FollowUpDto {

    private Long id;

    @NotNull(message = "El presupuesto es obligatorio")
    @Positive(message = "El identificador del presupuesto debe ser válido")
    private Long quoteId;

    @NotNull(message = "El tipo de seguimiento es obligatorio")
    private FollowUpType type;

    private FollowUpStatus status;

    @NotNull(message = "La fecha programada es obligatoria")
    private LocalDateTime scheduledAt;

    private LocalDateTime completedAt;

    @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
    private String notes;

    @Size(max = 500, message = "El resultado no puede superar los 500 caracteres")
    private String result;

    private Boolean automated;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }

    public FollowUpType getType() {
        return type;
    }

    public void setType(FollowUpType type) {
        this.type = type;
    }

    public FollowUpStatus getStatus() {
        return status;
    }

    public void setStatus(FollowUpStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Boolean getAutomated() {
        return automated;
    }

    public void setAutomated(Boolean automated) {
        this.automated = automated;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}