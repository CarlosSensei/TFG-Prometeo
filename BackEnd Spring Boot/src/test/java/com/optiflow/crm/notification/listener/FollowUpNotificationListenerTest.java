package com.optiflow.crm.notification.listener;

import com.optiflow.crm.followup.FollowUpRetryService;
import com.optiflow.crm.followup.event.FollowUpDueEvent;
import com.optiflow.crm.notification.model.NotificationService;
import com.optiflow.crm.notification.model.NotificationType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowUpNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private FollowUpRetryService retryService;

    @InjectMocks
    private FollowUpNotificationListener listener;

    // =====================================
    // 1. NOTIFICACIÓN CORRECTA
    // =====================================

    @Test
    void givenDueEvent_whenHandle_thenCreateNotification() {

        // GIVEN
        FollowUpDueEvent event = createEvent();

        // WHEN
        listener.handleFollowUpDue(event);

        // THEN
        verify(notificationService).createFollowUpNotification(
                eq(10L),
                eq(5L),
                eq(NotificationType.FOLLOW_UP_DUE),
                anyString(),
                anyString()
        );

        // No debe activarse el mecanismo de recuperación.
        verifyNoInteractions(retryService);

        verifyNoMoreInteractions(notificationService);
    }

    // =====================================
    // 2. ERROR AL CREAR NOTIFICACIÓN
    // =====================================

    @Test
    void givenNotificationFailure_whenHandle_thenReleaseForRetry() {

        // GIVEN
        FollowUpDueEvent event = createEvent();

        IllegalStateException simulatedError =
                new IllegalStateException("Fallo simulado de notificación");

        when(notificationService.createFollowUpNotification(
                eq(10L),
                eq(5L),
                eq(NotificationType.FOLLOW_UP_DUE),
                anyString(),
                anyString()
        )).thenThrow(simulatedError);

        // WHEN
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> listener.handleFollowUpDue(event));

        // THEN
        assertSame(simulatedError, exception);

        verify(retryService).releaseForRetry(eq(10L), eq(event.scheduledAt()));
        verifyNoMoreInteractions(notificationService, retryService);
    }

    // =====================================
    // 3. ERROR DURANTE LA RECUPERACIÓN
    // =====================================

    @Test
    void givenNotificationAndRetryFailure_whenHandle_thenPreserveOriginalError() {

        // GIVEN
        FollowUpDueEvent event = createEvent();

        IllegalStateException notificationError =
                new IllegalStateException("Error creando notificación");

        IllegalStateException retryError =
                new IllegalStateException("Error liberando seguimiento");

        when(notificationService.createFollowUpNotification(
                eq(10L),
                eq(5L),
                eq(NotificationType.FOLLOW_UP_DUE),
                anyString(),
                anyString()
        )).thenThrow(notificationError);

        doThrow(retryError)
                .when(retryService)
                .releaseForRetry(eq(10L), eq(event.scheduledAt()));

        // WHEN
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listener.handleFollowUpDue(event)
        );

        // THEN
        // Se conserva el error original.
        assertSame(notificationError, exception);

        // El error de recuperación se añade como suprimido.
        assertEquals(1, exception.getSuppressed().length);
        assertSame(retryError, exception.getSuppressed()[0]);

        verify(notificationService).createFollowUpNotification(
                eq(10L),
                eq(5L),
                eq(NotificationType.FOLLOW_UP_DUE),
                anyString(),
                anyString()
        );

        verify(retryService).releaseForRetry(eq(10L), eq(event.scheduledAt()));
        verifyNoMoreInteractions(notificationService, retryService);
    }

    // =====================================
    // MÉTODO AUXILIAR
    // =====================================

    private FollowUpDueEvent createEvent() {

        return new FollowUpDueEvent(
                10L,
                5L,
                LocalDateTime.of(2026, 10, 20, 10, 0)
        );
    }
}
