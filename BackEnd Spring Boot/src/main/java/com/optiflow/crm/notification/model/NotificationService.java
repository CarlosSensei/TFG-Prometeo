package com.optiflow.crm.notification.model;

import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<Notification> findAll(Pageable pageable);

    Page<Notification> findUnread(Pageable pageable);

    long countUnread();

    Notification findById(Long id);

    Notification createFollowUpNotification(
            Long followUpId,
            Long quoteId,
            NotificationType type,
            String title,
            String message
    );

    Notification markAsRead(Long id);

    void delete(Long id);
}