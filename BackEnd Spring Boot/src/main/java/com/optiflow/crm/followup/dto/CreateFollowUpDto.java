
package com.optiflow.crm.followup.dto;

import com.optiflow.crm.followup.model.FollowUpType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateFollowUpDto {

    @NotNull(message = "El tipo de seguimiento es obligatorio")
    private FollowUpType type;

    @NotNull(message = "La fecha programada es obligatoria")
    private LocalDateTime scheduledAt;

    @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
    private String notes;

    private Boolean automated;

    public FollowUpType getType() { return type; }

    public void setType(FollowUpType type) { this.type = type; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }

    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getAutomated() { return automated; }

    public void setAutomated(Boolean automated) { this.automated = automated; }
}
