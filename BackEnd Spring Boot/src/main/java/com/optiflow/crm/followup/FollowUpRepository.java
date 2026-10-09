package com.optiflow.crm.followup;

import com.optiflow.crm.followup.model.FollowUp;

import com.optiflow.crm.followup.model.FollowUpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long>, JpaSpecificationExecutor<FollowUp> {

    Page<FollowUp> findByActiveTrue(Pageable pageable);

    Page<FollowUp> findByQuoteIdAndActiveTrue(Long quoteId, Pageable pageable);

    List<FollowUp> findByStatusAndScheduledAtLessThanEqualAndActiveTrue(
            FollowUpStatus status, LocalDateTime scheduledAt);

}