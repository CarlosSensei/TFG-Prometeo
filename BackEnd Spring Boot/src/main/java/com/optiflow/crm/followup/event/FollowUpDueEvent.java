package com.optiflow.crm.followup.event;

import java.time.LocalDateTime;

// Record para generar eventos de seguimiento
public record FollowUpDueEvent(Long followUpId, Long quoteId, LocalDateTime scheduledAt) {
}