package com.optiflow.crm.notification;

import com.optiflow.crm.notification.dto.NotificationDto;
import com.optiflow.crm.notification.mapper.NotificationMapper;
import com.optiflow.crm.notification.model.Notification;

import com.optiflow.crm.notification.model.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controler de notificaciones
@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    // Inyeccion del service y del mapper
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService,
            NotificationMapper notificationMapper) {

        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    // Listado paginado de notificaciones
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> findAll(Pageable pageable) {

        Page<NotificationDto> result = notificationService
                        .findAll(pageable).map(notificationMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Nusqueda de notificaciones no leidas
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationDto>> findUnread(Pageable pageable) {

        Page<NotificationDto> result = notificationService
                        .findUnread(pageable).map(notificationMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Numero de notificaciones no leidas
    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnread() {

        return ResponseEntity.ok(notificationService.countUnread());
    }

    // Metodo para marcar como leida una notificacion
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {

        Notification notification = notificationService.markAsRead(id);

        return ResponseEntity.ok(notificationMapper.toDto(notification));
    }

    // Borrar una notificacion
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        notificationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}