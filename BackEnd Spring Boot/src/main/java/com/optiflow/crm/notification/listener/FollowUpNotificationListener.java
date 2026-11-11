package com.optiflow.crm.notification.listener;

import com.optiflow.crm.followup.FollowUpRetryService;
import com.optiflow.crm.followup.event.FollowUpDueEvent;
import com.optiflow.crm.notification.model.NotificationService;
import com.optiflow.crm.notification.model.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FollowUpNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(FollowUpNotificationListener.class);

    private final NotificationService notificationService;
    private final FollowUpRetryService retryService;

    public FollowUpNotificationListener(
            NotificationService notificationService,
            FollowUpRetryService retryService) {

        this.notificationService = notificationService;
        this.retryService = retryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowUpDue(FollowUpDueEvent event) {

        try {

            notificationService.createFollowUpNotification(
                    event.followUpId(),
                    event.quoteId(),
                    NotificationType.FOLLOW_UP_DUE,
                    "Seguimiento pendiente",
                    "El seguimiento " + event.followUpId()
                            + " del presupuesto "
                            + event.quoteId()
                            + " está pendiente."
            );

        } catch (RuntimeException exception) {

            log.error("Error creando notificación del seguimiento {}",
                    event.followUpId(), exception);

            try {
                retryService.releaseForRetry(event.followUpId(), event.scheduledAt());

            } catch (RuntimeException recoveryException) {

                exception.addSuppressed(recoveryException);

                log.error("No se pudo liberar el seguimiento {} para reintento",
                        event.followUpId(), recoveryException);
            }

            throw exception;
        }
    }
}
