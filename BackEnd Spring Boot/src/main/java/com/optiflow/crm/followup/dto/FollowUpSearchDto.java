package com.optiflow.crm.followup.dto;

import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;

import java.time.LocalDateTime;

// Entidad dto de filtrado de seguimientos
public class FollowUpSearchDto {

    private Long quoteId;

    private FollowUpType type;

    private FollowUpStatus status;

    private LocalDateTime scheduledFrom;

    private LocalDateTime scheduledTo;

    private LocalDateTime completedFrom;

    private LocalDateTime completedTo;

    private Boolean automated;

    private Boolean active = true;

    private Integer page = 0;

    private Integer size = 10;

    private String sortBy = "scheduledAt";

    private String sortDirection = "asc";

    // Getters y Setters

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

    public LocalDateTime getScheduledFrom() {
        return scheduledFrom;
    }

    public void setScheduledFrom(LocalDateTime scheduledFrom) {
        this.scheduledFrom = scheduledFrom;
    }

    public LocalDateTime getScheduledTo() {
        return scheduledTo;
    }

    public void setScheduledTo(LocalDateTime scheduledTo) {
        this.scheduledTo = scheduledTo;
    }

    public LocalDateTime getCompletedFrom() {
        return completedFrom;
    }

    public void setCompletedFrom(LocalDateTime completedFrom) {
        this.completedFrom = completedFrom;
    }

    public LocalDateTime getCompletedTo() {
        return completedTo;
    }

    public void setCompletedTo(LocalDateTime completedTo) {
        this.completedTo = completedTo;
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

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}