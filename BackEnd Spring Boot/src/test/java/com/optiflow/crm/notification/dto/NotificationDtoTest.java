
package com.optiflow.crm.notification.dto;

import com.optiflow.crm.notification.model.NotificationType;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationDtoTest {

    // =====================================
    // 1. DTO CON TODOS LOS CAMPOS
    // =====================================

    @Test
    void givenNotificationData_whenSetFields_thenReturnValues() {

        // GIVEN
        NotificationDto dto = new NotificationDto();

        LocalDateTime readAt =
                LocalDateTime.of(2026, 10, 20, 11, 0);

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 10, 20, 10, 0);

        // WHEN
        dto.setId(1L);
        dto.setType(NotificationType.FOLLOW_UP_DUE);
        dto.setTitle("Seguimiento pendiente");
        dto.setMessage("Contactar con el cliente");
        dto.setFollowUpId(10L);
        dto.setQuoteId(5L);
        dto.setRead(true);
        dto.setReadAt(readAt);
        dto.setCreatedAt(createdAt);

        // THEN
        assertAll(
                () -> assertEquals(1L, dto.getId()),
                () -> assertEquals(NotificationType.FOLLOW_UP_DUE, dto.getType()),
                () -> assertEquals("Seguimiento pendiente", dto.getTitle()),
                () -> assertEquals("Contactar con el cliente", dto.getMessage()),
                () -> assertEquals(10L, dto.getFollowUpId()),
                () -> assertEquals(5L, dto.getQuoteId()),
                () -> assertTrue(dto.getRead()),
                () -> assertEquals(readAt, dto.getReadAt()),
                () -> assertEquals(createdAt, dto.getCreatedAt())
        );
    }

    // =====================================
    // 2. DTO RECIÉN CREADO
    // =====================================

    @Test
    void givenNewDto_whenCreated_thenAllFieldsAreNull() {

        // GIVEN / WHEN
        NotificationDto dto = new NotificationDto();

        // THEN
        assertAll(
                () -> assertNull(dto.getId()),
                () -> assertNull(dto.getType()),
                () -> assertNull(dto.getTitle()),
                () -> assertNull(dto.getMessage()),
                () -> assertNull(dto.getFollowUpId()),
                () -> assertNull(dto.getQuoteId()),
                () -> assertNull(dto.getRead()),
                () -> assertNull(dto.getReadAt()),
                () -> assertNull(dto.getCreatedAt())
        );
    }

    // =====================================
    // 3. NOTIFICACIÓN NO LEÍDA
    // =====================================

    @Test
    void givenUnreadNotification_whenBuildDto_thenReadAtIsNull() {

        // GIVEN
        NotificationDto dto = new NotificationDto();

        // WHEN
        dto.setRead(false);
        dto.setReadAt(null);

        // THEN
        assertFalse(dto.getRead());
        assertNull(dto.getReadAt());
    }
}
