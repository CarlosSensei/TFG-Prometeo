package com.optiflow.crm.followup;

import com.optiflow.crm.followup.model.FollowUp;

import com.optiflow.crm.followup.model.FollowUpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// Repository Jpa de seguimientos
public interface FollowUpRepository extends JpaRepository<FollowUp, Long>, JpaSpecificationExecutor<FollowUp> {

    Page<FollowUp> findByActiveTrue(Pageable pageable);

    Page<FollowUp> findByQuoteIdAndActiveTrue(Long quoteId, Pageable pageable);

    List<FollowUp> findByStatusAndScheduledAtLessThanEqualAndActiveTrue(
            FollowUpStatus status, LocalDateTime scheduledAt);

    List<FollowUp> findByStatusAndScheduledAtLessThanEqualAndActiveTrueAndDueEventPublishedAtIsNull(
            FollowUpStatus status, LocalDateTime scheduledAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE FollowUp f
        SET f.dueEventPublishedAt = :publishedAt
        WHERE f.id = :id
          AND f.status = :status
          AND f.active = true
          AND f.scheduledAt = :scheduledAt
          AND f.scheduledAt <= :publishedAt
          AND f.dueEventPublishedAt IS NULL
        """)
    int claimDueEvent(@Param("id") Long id,
                      @Param("status") FollowUpStatus status,
                      @Param("scheduledAt") LocalDateTime scheduledAt,
                      @Param("publishedAt") LocalDateTime publishedAt);
}