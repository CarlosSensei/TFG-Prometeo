package com.optiflow.crm.notification.listener;

import com.optiflow.crm.followup.event.FollowUpDueEvent;
import com.optiflow.crm.notification.model.NotificationService;
import com.optiflow.crm.notification.model.NotificationType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowUpNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FollowUpNotificationListener listener;

    @Test
    void givenDueEvent_whenHandle_thenCreateNotification() {

        // GIVEN
        FollowUpDueEvent event = new FollowUpDueEvent(10L, 5L,
                LocalDateTime.of(2026, 10, 20, 10, 0));

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

        verifyNoMoreInteractions(notificationService);
    }
}
