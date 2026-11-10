package com.optiflow.crm.notification;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationType;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock NotificationRepository notificationRepository;
    @Mock QuoteRepository quoteRepository;
    @Mock CurrentUserService currentUserService;
    @InjectMocks NotificationServiceImpl service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(20L);
    }

    private void authenticated() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    private Notification notification() {
        Notification n = new Notification();
        n.setRecipient(user);
        n.setType(NotificationType.FOLLOW_UP_DUE);
        n.setFollowUpId(10L);
        n.setQuoteId(5L);
        n.setTitle("Seguimiento pendiente");
        n.setMessage("Llamar");
        n.setRead(false);
        n.setActive(true);
        return n;
    }

    @Test void findAllOnlyReturnsCurrentUser() {
        authenticated();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification()), pageable, 1);
        when(notificationRepository.findByRecipientIdAndActiveTrue(20L, pageable)).thenReturn(page);
        assertSame(page, service.findAll(pageable));
        verify(notificationRepository).findByRecipientIdAndActiveTrue(20L, pageable);
    }

    @Test void unreadOnlyReturnsCurrentUser() {
        authenticated();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification()), pageable, 1);
        when(notificationRepository.findByRecipientIdAndReadFalseAndActiveTrue(20L, pageable)).thenReturn(page);
        assertSame(page, service.findUnread(pageable));
    }

    @Test void countOnlyCurrentUser() {
        authenticated();
        when(notificationRepository.countByRecipientIdAndReadFalseAndActiveTrue(20L)).thenReturn(3L);
        assertEquals(3L, service.countUnread());
    }

    @Test void findByIdChecksOwnership() {
        authenticated();
        Notification n = notification();
        when(notificationRepository.findByIdAndRecipientIdAndActiveTrue(10L, 20L)).thenReturn(Optional.of(n));
        assertSame(n, service.findById(10L));
    }

    @Test void cannotReadNotificationOwnedByAnotherUser() {
        authenticated();
        when(notificationRepository.findByIdAndRecipientIdAndActiveTrue(10L, 20L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.markAsRead(10L));
        verify(notificationRepository, never()).save(any());
    }

    @Test void createNotificationAssignsQuoteSellerWithoutSecurityContext() {
        Quote q = new Quote();
        q.setSeller(user);
        when(quoteRepository.findById(5L)).thenReturn(Optional.of(q));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));
        Notification n = service.createFollowUpNotification(10L, 5L,
                NotificationType.FOLLOW_UP_DUE, "Pendiente", "Llamar");
        assertSame(user, n.getRecipient());
        assertEquals(10L, n.getFollowUpId());
        verifyNoInteractions(currentUserService);
    }

    @Test void duplicateNotificationDoesNotSave() {
        when(notificationRepository.existsByFollowUpIdAndType(10L, NotificationType.FOLLOW_UP_DUE))
                .thenReturn(true);
        assertNull(service.createFollowUpNotification(10L, 5L,
                NotificationType.FOLLOW_UP_DUE, "Pendiente", "Llamar"));
        verify(notificationRepository, never()).save(any());
    }

    @Test void markAsReadUpdatesTimestamp() {
        authenticated();
        Notification n = notification();
        when(notificationRepository.findByIdAndRecipientIdAndActiveTrue(10L, 20L))
                .thenReturn(Optional.of(n));
        when(notificationRepository.save(n)).thenReturn(n);
        service.markAsRead(10L);
        assertTrue(n.getRead());
        assertNotNull(n.getReadAt());
    }

    @Test void alreadyReadDoesNotUpdateTimestamp() {
        authenticated();
        Notification n = notification();
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 1, 10, 0);
        n.setRead(true);
        n.setReadAt(timestamp);
        when(notificationRepository.findByIdAndRecipientIdAndActiveTrue(10L, 20L))
                .thenReturn(Optional.of(n));
        assertSame(n, service.markAsRead(10L));
        assertEquals(timestamp, n.getReadAt());
        verify(notificationRepository, never()).save(any());
    }

    @Test void deleteOnlyOwnNotification() {
        authenticated();
        Notification n = notification();
        when(notificationRepository.findByIdAndRecipientIdAndActiveTrue(10L, 20L))
                .thenReturn(Optional.of(n));
        service.delete(10L);
        assertFalse(n.getActive());
        verify(notificationRepository).save(n);
    }
}
