package com.optiflow.crm.followup;

import com.optiflow.crm.followup.event.FollowUpDueEvent;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.quote.model.Quote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowUpSchedulerTest {

    @Mock
    private FollowUpRepository followUpRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FollowUpScheduler scheduler;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private FollowUp createFollowUp(
            Long followUpId,
            Long quoteId,
            LocalDateTime scheduledAt) {

        Quote quote = new Quote();
        quote.setId(quoteId);

        FollowUp followUp = new FollowUp();

        followUp.setId(followUpId);
        followUp.setQuote(quote);
        followUp.setStatus(FollowUpStatus.PENDING);
        followUp.setScheduledAt(scheduledAt);
        followUp.setActive(true);
        followUp.setDueEventPublishedAt(null);

        return followUp;
    }

    private LocalDateTime scheduledDate() {
        return LocalDateTime.of(2026, 10, 1, 10, 0);
    }

    // =====================================
    // 1. UN SEGUIMIENTO VENCIDO
    // =====================================

    @Test
    void givenOneDueFollowUp_whenSchedulerRuns_thenPublishEvent() {

        // GIVEN
        LocalDateTime scheduledAt = scheduledDate();

        FollowUp followUp = createFollowUp(10L, 5L, scheduledAt);

        when(followUpRepository
                .findByStatusAndScheduledAtLessThanEqualAndActiveTrueAndDueEventPublishedAtIsNull(
                        eq(FollowUpStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(followUp));

        // Simulamos una reclamación exitosa.
        when(followUpRepository.claimDueEvent(
                eq(10L),
                eq(FollowUpStatus.PENDING),
                eq(scheduledAt),
                any(LocalDateTime.class)
        )).thenReturn(1);

        // WHEN
        scheduler.checkPendingFollowUps();

        // THEN
        ArgumentCaptor<FollowUpDueEvent> captor = ArgumentCaptor.forClass(FollowUpDueEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        FollowUpDueEvent event = captor.getValue();

        assertAll(
                () -> assertEquals(10L, event.followUpId()),
                () -> assertEquals(5L, event.quoteId()),
                () -> assertEquals(scheduledAt, event.scheduledAt()));

        verify(followUpRepository).claimDueEvent(eq(10L),
                eq(FollowUpStatus.PENDING), eq(scheduledAt), any(LocalDateTime.class));

        verify(eventPublisher, times(1))
                .publishEvent(any(FollowUpDueEvent.class));
    }

    // =====================================
    // 2. SIN SEGUIMIENTOS VENCIDOS
    // =====================================

    @Test
    void givenNoDueFollowUps_whenSchedulerRuns_thenPublishNothing() {

        // GIVEN
        when(followUpRepository
                .findByStatusAndScheduledAtLessThanEqualAndActiveTrueAndDueEventPublishedAtIsNull(
                        eq(FollowUpStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // WHEN
        scheduler.checkPendingFollowUps();

        // THEN
        verifyNoInteractions(eventPublisher);

        verify(followUpRepository, never()).claimDueEvent(
                anyLong(),
                any(FollowUpStatus.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    // =====================================
    // 3. VARIOS SEGUIMIENTOS VENCIDOS
    // =====================================

    @Test
    void givenMultipleDueFollowUps_whenSchedulerRuns_thenPublishEach() {

        // GIVEN
        LocalDateTime firstDate = LocalDateTime
                .of(2026, 10, 1, 10, 0);
        LocalDateTime secondDate = LocalDateTime
                .of(2026, 10, 2, 11, 0);
        LocalDateTime thirdDate = LocalDateTime
                .of(2026, 10, 3, 12, 0);

        FollowUp first = createFollowUp(1L, 10L, firstDate);
        FollowUp second = createFollowUp(2L, 20L, secondDate);
        FollowUp third = createFollowUp(3L, 30L, thirdDate);

        when(followUpRepository
                .findByStatusAndScheduledAtLessThanEqualAndActiveTrueAndDueEventPublishedAtIsNull(
                        eq(FollowUpStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(first, second, third));

        // Cada seguimiento se reclama correctamente.
        when(followUpRepository.claimDueEvent(
                anyLong(),
                eq(FollowUpStatus.PENDING),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(1);

        // WHEN
        scheduler.checkPendingFollowUps();

        // THEN
        ArgumentCaptor<FollowUpDueEvent> captor = ArgumentCaptor.forClass(FollowUpDueEvent.class);

        verify(eventPublisher, times(3)).publishEvent(captor.capture());

        List<FollowUpDueEvent> events = captor.getAllValues();

        assertEquals(3, events.size());

        assertAll(
                () -> assertEquals(1L, events.get(0).followUpId()),
                () -> assertEquals(10L, events.get(0).quoteId()),
                () -> assertEquals(firstDate, events.get(0).scheduledAt()),
                () -> assertEquals(2L, events.get(1).followUpId()),
                () -> assertEquals(20L, events.get(1).quoteId()),
                () -> assertEquals(secondDate, events.get(1).scheduledAt()),
                () -> assertEquals(3L, events.get(2).followUpId()),
                () -> assertEquals(30L, events.get(2).quoteId()),
                () -> assertEquals(thirdDate, events.get(2).scheduledAt()));

        verify(followUpRepository, times(3))
                .claimDueEvent(
                        anyLong(),
                        eq(FollowUpStatus.PENDING),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    // =====================================
    // 4. CONSULTA CON LA FECHA ACTUAL
    // =====================================

    @Test
    void givenSchedulerExecution_whenQueryRepository_thenUseCurrentTime() {

        // GIVEN
        when(followUpRepository
                .findByStatusAndScheduledAtLessThanEqualAndActiveTrueAndDueEventPublishedAtIsNull(
                        eq(FollowUpStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        LocalDateTime before = LocalDateTime.now();

        // WHEN
        scheduler.checkPendingFollowUps();

        LocalDateTime after = LocalDateTime.now();

        // THEN
        ArgumentCaptor<LocalDateTime> dateCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        verify(followUpRepository)
                .findByStatusAndScheduledAtLessThanEqualAndActiveTrueAndDueEventPublishedAtIsNull(
                        eq(FollowUpStatus.PENDING), dateCaptor.capture());

        LocalDateTime queriedAt = dateCaptor.getValue();

        assertFalse(queriedAt.isBefore(before));
        assertFalse(queriedAt.isAfter(after));

        verifyNoInteractions(eventPublisher);
    }

    // =====================================
    // 5. DOS EJECUCIONES, UN ÚNICO EVENTO
    // =====================================

    @Test
    void givenSamePendingFollowUp_whenSchedulerRunsTwice_thenPublishOnce() {

        // GIVEN
        FollowUp followUp = createFollowUp(10L, 5L, scheduledDate());

        when(followUpRepository
                .findByStatusAndScheduledAtLessThanEqualAndActiveTrueAndDueEventPublishedAtIsNull(
                        eq(FollowUpStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(followUp));

        // Primera reclamación: 1 fila modificada.
        // Segunda reclamación: 0 filas modificadas.
        when(followUpRepository.claimDueEvent(
                eq(10L),
                eq(FollowUpStatus.PENDING),
                eq(followUp.getScheduledAt()),
                any(LocalDateTime.class)
        )).thenReturn(1, 0);

        // WHEN
        scheduler.checkPendingFollowUps();
        scheduler.checkPendingFollowUps();

        // THEN
        verify(eventPublisher, times(1))
                .publishEvent(any(FollowUpDueEvent.class));

        verify(followUpRepository, times(2))
                .claimDueEvent(
                        eq(10L),
                        eq(FollowUpStatus.PENDING),
                        eq(followUp.getScheduledAt()),
                        any(LocalDateTime.class)
                );
    }

    // =====================================
    // 6. OTRO PROCESO YA LO HA RECLAMADO
    // =====================================

    @Test
    void givenAlreadyClaimedFollowUp_whenSchedulerRuns_thenDoNotPublish() {

        // GIVEN
        FollowUp followUp =
                createFollowUp(10L, 5L, scheduledDate());

        when(followUpRepository
                .findByStatusAndScheduledAtLessThanEqualAndActiveTrueAndDueEventPublishedAtIsNull(
                        eq(FollowUpStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(followUp));

        // El repositorio indica que no se ha
        // podido reclamar el seguimiento.
        when(followUpRepository.claimDueEvent(
                eq(10L),
                eq(FollowUpStatus.PENDING),
                eq(followUp.getScheduledAt()),
                any(LocalDateTime.class)
        )).thenReturn(0);

        // WHEN
        scheduler.checkPendingFollowUps();

        // THEN
        verify(followUpRepository).claimDueEvent(
                eq(10L),
                eq(FollowUpStatus.PENDING),
                eq(followUp.getScheduledAt()),
                any(LocalDateTime.class)
        );

        verifyNoInteractions(eventPublisher);
    }
}
