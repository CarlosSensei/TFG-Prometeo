package com.optiflow.crm.notification.model;

import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Interface del service de notificaciones
public interface NotificationService {

    // Busqueda de una pagina de notificaciones
    Page<Notification> findAll(Pageable pageable);

    // Busqueda de no leidas
    Page<Notification> findUnread(Pageable pageable);

    // Cuenta de no leidas
    long countUnread();

    // Busqueda por id
    Notification findById(Long id);

    // crea notificacion asociada a un seguimiento
    Notification createFollowUpNotification(
            Long followUpId,
            Long quoteId,
            NotificationType type,
            String title,
            String message
    );

    // Marcar como leido
    Notification markAsRead(Long id);

    // Borrar una notificacion
    void delete(Long id);
}