package com.optiflow.crm.followup.event;

import java.time.LocalDateTime;

public record FollowUpDueEvent(Long followUpId, Long quoteId, LocalDateTime scheduledAt) {
}