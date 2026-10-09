package com.optiflow.crm.followup.dto;

import jakarta.validation.constraints.Size;

public class CompleteFollowUpDto {

    @Size(max = 500)
    private String result;

    @Size(max = 1000)
    private String notes;

    // Getters y Setters

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}