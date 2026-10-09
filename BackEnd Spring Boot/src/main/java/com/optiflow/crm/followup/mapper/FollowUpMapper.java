package com.optiflow.crm.followup.mapper;

import com.optiflow.crm.followup.dto.FollowUpDto;
import com.optiflow.crm.followup.model.FollowUp;

import org.springframework.stereotype.Component;

@Component
public class FollowUpMapper {

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