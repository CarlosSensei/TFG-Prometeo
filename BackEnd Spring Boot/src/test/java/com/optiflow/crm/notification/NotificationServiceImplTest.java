package com.optiflow.crm.notification;

import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl service;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Notification createNotification() {

        Notification notification = new Notification();

        notification.setFollowUpId(10L);
        notification.setQuoteId(5L);
        notification.setType(NotificationType.FOLLOW_UP_DUE);

        notification.setTitle("Seguimiento pendiente");
        notification.setMessage("Contactar con el cliente");

        notification.setRead(false);
        notification.setActive(true);

        return notification;
    }

    // =====================================
    // 1. FIND ALL
    // =====================================

    @Test
    void givenActiveNotifications_whenFindAll_thenReturnPage() {

        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);

        Notification notification = createNotification();

        Page<Notification> expected = new PageImpl<>(
                List.of(notification), pageable, 1);

        when(notificationRepository.findByActiveTrue(pageable)).thenReturn(expected);

        // WHEN
        Page<Notification> result = service.findAll(pageable);

        // THEN
        assertSame(expected, result);
        assertEquals(1, result.getTotalElements());

        verify(notificationRepository).findByActiveTrue(pageable);
        verify(notificationRepository, never()).findAll(pageable);
    }

    // =====================================
    // 2. FIND UNREAD
    // =====================================

    @Test
    void givenUnreadNotifications_whenFindUnread_thenReturnPage() {

        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);

        Notification notification = createNotification();

        Page<Notification> expected = new PageImpl<>(
                List.of(notification), pageable, 1);

        when(notificationRepository
                .findByReadFalseAndActiveTrue(pageable))
                .thenReturn(expected);

        // WHEN
        Page<Notification> result = service.findUnread(pageable);

        // THEN
        assertSame(expected, result);
        assertEquals(1, result.getTotalElements());

        verify(notificationRepository).findByReadFalseAndActiveTrue(pageable);
    }

    // =====================================
    // 3. COUNT UNREAD
    // =====================================

    @Test
    void givenUnreadNotifications_whenCountUnread_thenReturnCount() {

        // GIVEN
        when(notificationRepository.countByReadFalseAndActiveTrue())
                .thenReturn(3L);

        // WHEN
        long result = service.countUnread();

        // THEN
        assertEquals(3L, result);

        verify(notificationRepository).countByReadFalseAndActiveTrue();
    }

    // =====================================
    // 4. FIND BY ID
    // =====================================

    @Test
    void givenExistingNotification_whenFindById_thenReturnNotification() {

        // GIVEN
        Notification notification = createNotification();

        when(notificationRepository.findById(10L))
                .thenReturn(Optional.of(notification));

        // WHEN
        Notification result = service.findById(10L);

        // THEN
        assertSame(notification, result);

        verify(notificationRepository).findById(10L);
    }

    // =====================================
    // 5. FIND BY ID NOT FOUND
    // =====================================

    @Test
    void givenUnknownId_whenFindById_thenThrowException() {

        // GIVEN
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.findById(99L));

        assertEquals("Notificación no encontrada con id: 99",
                exception.getMessage());

        verify(notificationRepository).findById(99L);
    }

    // =====================================
    // 6. CREATE FOLLOWUP NOTIFICATION
    // =====================================

    @Test
    void givenNewFollowUp_whenCreateNotification_thenSaveAllFields() {

        // GIVEN
        Long followUpId = 10L;
        Long quoteId = 5L;

        NotificationType type = NotificationType.FOLLOW_UP_DUE;

        String title = "Seguimiento pendiente";
        String message = "Contactar con el cliente";

        when(notificationRepository.existsByFollowUpIdAndType(followUpId, type))
                .thenReturn(false);

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Notification result = service.createFollowUpNotification(
                followUpId,
                quoteId,
                type,
                title,
                message
        );

        // THEN
        assertNotNull(result);

        assertAll(
                () -> assertEquals(followUpId, result.getFollowUpId()),
                () -> assertEquals(quoteId, result.getQuoteId()),
                () -> assertEquals(type, result.getType()),
                () -> assertEquals(title, result.getTitle()),
                () -> assertEquals(message, result.getMessage()),
                () -> assertFalse(result.getRead()),
                () -> assertTrue(result.getActive()),
                () -> assertNull(result.getReadAt())
        );

        verify(notificationRepository).existsByFollowUpIdAndType(followUpId, type);
        verify(notificationRepository).save(result);
    }

    // =====================================
    // 7. DUPLICATE NOTIFICATION
    // =====================================

    @Test
    void givenExistingFollowUpNotification_whenCreate_thenDoNotSave() {

        // GIVEN
        when(notificationRepository.existsByFollowUpIdAndType(10L,
                NotificationType.FOLLOW_UP_DUE)).thenReturn(true);

        // WHEN
        Notification result = service.createFollowUpNotification(
                10L,
                5L,
                NotificationType.FOLLOW_UP_DUE,
                "Seguimiento pendiente",
                "Contactar con el cliente"
        );

        // THEN
        assertNull(result);

        verify(notificationRepository).existsByFollowUpIdAndType(10L,
                NotificationType.FOLLOW_UP_DUE);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    // =====================================
    // 8. DIFFERENT NOTIFICATION TYPE
    // =====================================

    @Test
    void givenDifferentNotificationType_whenCreate_thenAllowCreation() {

        // GIVEN
        NotificationType type = NotificationType.FOLLOW_UP_OVERDUE;

        when(notificationRepository.existsByFollowUpIdAndType(10L, type))
                .thenReturn(false);

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Notification result = service.createFollowUpNotification(
                10L,
                5L,
                type,
                "Seguimiento vencido",
                "El seguimiento ha superado la fecha prevista"
        );

        // THEN
        assertNotNull(result);
        assertEquals(NotificationType.FOLLOW_UP_OVERDUE, result.getType());
        assertEquals(10L, result.getFollowUpId());

        verify(notificationRepository).save(result);
    }

    // =====================================
    // 9. MARK AS READ
    // =====================================

    @Test
    void givenUnreadNotification_whenMarkAsRead_thenSetReadAndDate() {

        // GIVEN
        Notification notification = createNotification();

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        LocalDateTime before = LocalDateTime.now();

        // WHEN
        Notification result = service.markAsRead(10L);

        LocalDateTime after = LocalDateTime.now();

        // THEN
        assertSame(notification, result);
        assertTrue(result.getRead());
        assertNotNull(result.getReadAt());
        assertFalse(result.getReadAt().isBefore(before));
        assertFalse(result.getReadAt().isAfter(after));

        verify(notificationRepository).save(notification);
    }

    // =====================================
    // 10. MARK AS READ - NOT FOUND
    // =====================================

    @Test
    void givenUnknownNotification_whenMarkAsRead_thenThrow() {

        // GIVEN
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.markAsRead(99L));

        assertEquals("Notificación no encontrada con id: 99",
                exception.getMessage());

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    // =====================================
    // 11. MARK ALREADY READ
    // =====================================


    @Test
    void givenAlreadyReadNotification_whenMarkAsRead_thenPreserveOriginalReadAt() {

        // GIVEN
        Notification notification = createNotification();

        LocalDateTime originalReadAt =
                LocalDateTime.of(2026, 10, 1, 10, 0);

        notification.setRead(true);
        notification.setReadAt(originalReadAt);

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        // WHEN
        Notification result = service.markAsRead(10L);

        // THEN
        assertAll(
                () -> assertSame(notification, result),
                () -> assertTrue(result.getRead()),
                () -> assertEquals(originalReadAt, result.getReadAt()));

        verify(notificationRepository).findById(10L);

        // No debe volver a guardar la notificación.
        verify(notificationRepository, never()).save(any(Notification.class));
        verifyNoMoreInteractions(notificationRepository);
    }


    // =====================================
    // 12. SOFT DELETE
    // =====================================

    @Test
    void givenExistingNotification_whenDelete_thenDeactivate() {

        // GIVEN
        Notification notification = createNotification();

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        // WHEN
        service.delete(10L);

        // THEN
        assertFalse(notification.getActive());

        verify(notificationRepository).save(notification);
        verify(notificationRepository, never()).delete(any(Notification.class));
        verify(notificationRepository, never()).deleteById(anyLong());
    }

    // =====================================
    // 13. DELETE NOT FOUND
    // =====================================

    @Test
    void givenUnknownNotification_whenDelete_thenThrow() {

        // GIVEN
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.delete(99L));

        assertEquals("Notificación no encontrada con id: 99",
                exception.getMessage());

        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
