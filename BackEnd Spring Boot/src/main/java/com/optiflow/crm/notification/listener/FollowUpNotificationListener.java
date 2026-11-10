package com.optiflow.crm.notification.listener;

import com.optiflow.crm.followup.event.FollowUpDueEvent;
import com.optiflow.crm.notification.model.NotificationService;
import com.optiflow.crm.notification.model.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FollowUpNotificationListener {
    private final NotificationService notificationService;

    public FollowUpNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowUpDue(FollowUpDueEvent event) {
        notificationService.createFollowUpNotification(
                event.followUpId(), event.quoteId(), NotificationType.FOLLOW_UP_DUE,
                "Seguimiento pendiente",
                "El seguimiento " + event.followUpId() + " del presupuesto "
                        + event.quoteId() + " está pendiente.");
    }
}
