package com.optiflow.crm.notification;

import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientIdAndActiveTrue(Long recipientId, Pageable pageable);
    Page<Notification> findByRecipientIdAndReadFalseAndActiveTrue(Long recipientId, Pageable pageable);
    long countByRecipientIdAndReadFalseAndActiveTrue(Long recipientId);
    Optional<Notification> findByIdAndRecipientIdAndActiveTrue(Long id, Long recipientId);
    boolean existsByFollowUpIdAndType(Long followUpId, NotificationType type);
}
