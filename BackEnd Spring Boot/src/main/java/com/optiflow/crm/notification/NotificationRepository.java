package com.optiflow.crm.notification;

import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// Interfaz del repositorio Jpa de notificacion
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByActiveTrue(Pageable pageable);

    Page<Notification> findByReadFalseAndActiveTrue(Pageable pageable);

    boolean existsByFollowUpIdAndType(Long followUpId, NotificationType type);

    long countByReadFalseAndActiveTrue();
}