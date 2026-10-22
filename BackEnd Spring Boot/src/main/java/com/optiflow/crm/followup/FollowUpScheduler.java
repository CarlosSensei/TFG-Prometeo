package com.optiflow.crm.followup;

import com.optiflow.crm.followup.event.FollowUpDueEvent;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FollowUpScheduler {

    private final FollowUpRepository followUpRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FollowUpScheduler(
            FollowUpRepository followUpRepository,
            ApplicationEventPublisher eventPublisher) {

        this.followUpRepository = followUpRepository;
        this.eventPublisher = eventPublisher;
    }

    // Revisa los seguimientos cada hora.
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkPendingFollowUps() {

        LocalDateTime now = LocalDateTime.now();

        // 1. Buscar solamente seguimientos
        // que todavía no han generado un evento.
        List<FollowUp> followUps = followUpRepository
                .findByStatusAndScheduledAtLessThanEqualAndActiveTrueAndDueEventPublishedAtIsNull(
                        FollowUpStatus.PENDING, now);

        // 2. Procesar cada seguimiento.
        for (FollowUp followUp : followUps) {

            // Reclamarlo de forma atómica.
            int claimed = followUpRepository.claimDueEvent(
                    followUp.getId(), FollowUpStatus.PENDING,
                    followUp.getScheduledAt(), now);

            // Otro proceso ya lo reclamó,
            // o dejó de cumplir las condiciones.
            if (claimed != 1) {
                continue;
            }

            // 3. Publicar el evento únicamente
            // cuando la reclamación ha tenido éxito.
            FollowUpDueEvent event = new FollowUpDueEvent(
                    followUp.getId(),
                    followUp.getQuote().getId(),
                    followUp.getScheduledAt()
            );

            eventPublisher.publishEvent(event);
        }
    }
}
