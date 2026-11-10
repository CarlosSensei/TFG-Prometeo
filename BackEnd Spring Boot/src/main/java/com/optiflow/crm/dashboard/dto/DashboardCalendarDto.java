package com.optiflow.crm.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardCalendarDto(
        int year,
        int month,
        List<CalendarActionDto> monthActions,
        List<CalendarActionDto> nextActions
) {
    public record CalendarActionDto(
            Long id,
            Long quoteId,
            String quoteNumber,
            String type,
            LocalDateTime scheduledAt,
            boolean overdue
    ) {}
}
