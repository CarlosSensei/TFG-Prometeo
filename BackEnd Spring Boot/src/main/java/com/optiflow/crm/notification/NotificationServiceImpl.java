package com.optiflow.crm.notification;

import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationService;
import com.optiflow.crm.notification.model.NotificationType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Implementacion del service de notificaciones
@Service
public class NotificationServiceImpl implements NotificationService {

    // Inyeccion del repository
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {

        this.notificationRepository = notificationRepository;
    }

    // Busqueda paginada de notificaciones
    @Override
    public Page<Notification> findAll(Pageable pageable) {

        return notificationRepository.findByActiveTrue(pageable);
    }

    // Busqueda de notificaciones No Leidas
    @Override
    public Page<Notification> findUnread(Pageable pageable) {

        return notificationRepository.findByReadFalseAndActiveTrue(pageable);
    }

    // Numero de notificaciones no leidas
    @Override
    public long countUnread() {

        return notificationRepository.countByReadFalseAndActiveTrue();
    }

    // Busqueda por Id
    @Override
    public Notification findById(Long id) {

        return notificationRepository.findById(id).orElseThrow(() ->
                        new RuntimeException("Notificación no encontrada con id: " + id));
    }

    // Creacion de notificaciones asociadas a seguimientos
    @Override
    @Transactional
    public Notification createFollowUpNotification(
            Long followUpId,
            Long quoteId,
            NotificationType type,
            String title,
            String message
    ) {

        boolean exists =
                notificationRepository.existsByFollowUpIdAndType(followUpId, type);

        if (exists) { return null; }

        Notification notification = new Notification();

        notification.setFollowUpId(followUpId);
        notification.setQuoteId(quoteId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setActive(true);

        return notificationRepository.save(notification);
    }

    // Metodo para marcar una notificacion como leida
    @Override
    @Transactional
    public Notification markAsRead(Long id) {

        Notification notification = findById(id);

        if (Boolean.TRUE.equals(notification.getRead())) {
            return notification;
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    // Borrar una notificacion desactivandola
    @Override
    @Transactional
    public void delete(Long id) {

        Notification notification = findById(id);

        notification.setActive(false);

        notificationRepository.save(notification);
    }
}