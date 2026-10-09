package com.optiflow.crm.notification;

import com.optiflow.crm.notification.dto.NotificationDto;
import com.optiflow.crm.notification.mapper.NotificationMapper;
import com.optiflow.crm.notification.model.Notification;

import com.optiflow.crm.notification.model.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService,
            NotificationMapper notificationMapper) {

        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> findAll(Pageable pageable) {

        Page<NotificationDto> result = notificationService
                        .findAll(pageable).map(notificationMapper::toDto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationDto>> findUnread(Pageable pageable) {

        Page<NotificationDto> result = notificationService
                        .findUnread(pageable).map(notificationMapper::toDto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnread() {

        return ResponseEntity.ok(notificationService.countUnread());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {

        Notification notification = notificationService.markAsRead(id);

        return ResponseEntity.ok(notificationMapper.toDto(notification));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        notificationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}