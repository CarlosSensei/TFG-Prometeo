package com.optiflow.crm.followup.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FollowUpDueEventTest {

    // =====================================
    // 1. CREACIÓN DEL EVENTO
    // =====================================

    @Test
    void givenValidData_whenCreateEvent_thenStoreAllFields() {

        // GIVEN
        Long followUpId = 10L;
        Long quoteId = 5L;

        LocalDateTime scheduledAt = LocalDateTime
                .of(2026, 11, 10, 10, 0);

        // WHEN
        FollowUpDueEvent event = new FollowUpDueEvent(
                followUpId,
                quoteId,
                scheduledAt
        );

        // THEN
        assertAll(
                () -> assertEquals(10L, event.followUpId()),
                () -> assertEquals(5L, event.quoteId()),
                () -> assertEquals(scheduledAt, event.scheduledAt())
        );
    }

    // =====================================
    // 2. IGUALDAD ENTRE EVENTOS
    // =====================================

    @Test
    void givenSameData_whenCompareEvents_thenAreEqual() {

        // GIVEN
        LocalDateTime date = LocalDateTime
                .of(2026, 11, 10, 10, 0);

        FollowUpDueEvent first = new FollowUpDueEvent(10L, 5L, date);
        FollowUpDueEvent second = new FollowUpDueEvent(10L, 5L, date);

        // WHEN + THEN
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    // =====================================
    // 3. EVENTOS DIFERENTES
    // =====================================

    @Test
    void givenDifferentFollowUpIds_whenCompare_thenNotEqual() {

        // GIVEN
        LocalDateTime date = LocalDateTime
                .of(2026, 11, 10, 10, 0);

        FollowUpDueEvent first = new FollowUpDueEvent(10L, 5L, date);
        FollowUpDueEvent second = new FollowUpDueEvent(11L, 5L, date);

        // WHEN + THEN
        assertNotEquals(first, second);
    }

    // =====================================
    // 4. REPRESENTACIÓN DEL EVENTO
    // =====================================

    @Test
    void givenEvent_whenToString_thenContainFieldInformation() {

        // GIVEN
        FollowUpDueEvent event = new FollowUpDueEvent(10L, 5L,
                LocalDateTime.of(2026, 11, 10, 10, 0));

        // WHEN
        String result = event.toString();

        // THEN
        assertAll(
                () -> assertTrue(result.contains("followUpId=10")),
                () -> assertTrue(result.contains("quoteId=5")),
                () -> assertTrue(result.contains("scheduledAt="))
        );
    }
}
