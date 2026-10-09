package com.optiflow.crm.followup;

import com.optiflow.crm.followup.event.FollowUpDueEvent;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FollowUpScheduler {

    private final FollowUpRepository followUpRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FollowUpScheduler(FollowUpRepository followUpRepository,
                             ApplicationEventPublisher eventPublisher) {

        this.followUpRepository = followUpRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkPendingFollowUps() {

        LocalDateTime now = LocalDateTime.now();

        List<FollowUp> followUps =
                followUpRepository
                        .findByStatusAndScheduledAtLessThanEqualAndActiveTrue(
                                FollowUpStatus.PENDING, now);

        for (FollowUp followUp : followUps) {

            FollowUpDueEvent event =
                    new FollowUpDueEvent(
                            followUp.getId(),
                            followUp.getQuote().getId(),
                            followUp.getScheduledAt()
                    );

            eventPublisher.publishEvent(event);
        }
    }
}