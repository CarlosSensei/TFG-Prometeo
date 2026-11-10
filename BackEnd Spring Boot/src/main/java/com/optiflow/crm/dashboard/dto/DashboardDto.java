package com.optiflow.crm.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardDto(
        long activeClients,
        long openQuotes,
        long activeOrders,
        long pendingFollowUps,
        long overdueFollowUps,
        long convertedQuotes,
        long decidedQuotes,
        BigDecimal conversionRate,
        BigDecimal deliveredRevenue,
        List<ActivityDto> recentActivity,
        List<FollowUpSummaryDto> upcomingFollowUps
) {
    public record ActivityDto(
            String type, Long id, String reference,
            String status, LocalDateTime occurredAt
    ) {}

    public record FollowUpSummaryDto(
            Long id, Long quoteId, String quoteNumber,
            String type, LocalDateTime scheduledAt, boolean overdue
    ) {}
}
