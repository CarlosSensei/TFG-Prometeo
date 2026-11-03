package com.optiflow.crm.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClientTimelineEventDto(
        String id,
        String type,
        Long referenceId,
        LocalDateTime date,
        String title,
        String description,
        String status,
        BigDecimal amount
) {}
