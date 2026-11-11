package com.optiflow.crm.followup;

import com.optiflow.crm.followup.model.FollowUpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FollowUpRetryService {

    private final FollowUpRepository followUpRepository;

    public FollowUpRetryService(
            FollowUpRepository followUpRepository) {
        this.followUpRepository = followUpRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseForRetry(
            Long followUpId,
            LocalDateTime scheduledAt) {

        followUpRepository.releaseDueEvent(
                followUpId,
                scheduledAt,
                FollowUpStatus.PENDING
        );
    }
}
