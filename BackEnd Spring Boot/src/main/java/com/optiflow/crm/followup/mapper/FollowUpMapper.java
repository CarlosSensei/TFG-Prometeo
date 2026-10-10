package com.optiflow.crm.followup.mapper;

import com.optiflow.crm.followup.dto.FollowUpDto;
import com.optiflow.crm.followup.model.FollowUp;

import org.springframework.stereotype.Component;

// Mapper personalizado para seguimientos
@Component
public class FollowUpMapper {

    // Mapea de seguimiento a dto
    public FollowUpDto toDto(FollowUp followUp) {

        FollowUpDto dto = new FollowUpDto();

        dto.setId(followUp.getId());
        if (followUp.getQuote() != null) {
            dto.setQuoteId(followUp.getQuote().getId());
        }
        dto.setType(followUp.getType());
        dto.setStatus(followUp.getStatus());
        dto.setScheduledAt(followUp.getScheduledAt());
        dto.setCompletedAt(followUp.getCompletedAt());
        dto.setNotes(followUp.getNotes());
        dto.setResult(followUp.getResult());
        dto.setAutomated(followUp.getAutomated());
        dto.setActive(followUp.getActive());
        dto.setCreatedAt(followUp.getCreatedAt());
        dto.setUpdatedAt(followUp.getUpdatedAt());

        return dto;
    }

    // Mapea de Dto a entidad de seguimiento
    public FollowUp toEntity(FollowUpDto dto) {

        FollowUp followUp = new FollowUp();

        followUp.setType(dto.getType());
        followUp.setStatus(dto.getStatus());
        followUp.setScheduledAt(dto.getScheduledAt());
        followUp.setCompletedAt(dto.getCompletedAt());
        followUp.setNotes(dto.getNotes());
        followUp.setResult(dto.getResult());
        followUp.setAutomated(dto.getAutomated());

        return followUp;
    }

    // Actualiza una entidad seguimiento ya creada
    public void updateEntity(FollowUpDto dto, FollowUp followUp) {

        followUp.setType(dto.getType());
        followUp.setStatus(dto.getStatus());
        followUp.setScheduledAt(dto.getScheduledAt());
        followUp.setCompletedAt(dto.getCompletedAt());
        followUp.setNotes(dto.getNotes());
        followUp.setResult(dto.getResult());
        followUp.setAutomated(dto.getAutomated());
    }
}