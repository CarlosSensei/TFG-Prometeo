package com.optiflow.crm.client.dto;

import java.time.LocalDateTime;

// Items de la linea de tiempo de un cliente
public class ClientTimeLineItemDto {

    private LocalDateTime date;

    private String type;

    private Long referenceId;

    private String title;

    private String description;

    // Getters y Setters

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) { this.date = date; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
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
}