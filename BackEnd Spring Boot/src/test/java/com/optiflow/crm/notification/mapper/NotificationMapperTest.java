
package com.optiflow.crm.notification.mapper;

import com.optiflow.crm.notification.dto.NotificationDto;
import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    private NotificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMapper();
    }


    private Notification createNotification() {

        Notification notification = new Notification();

        // GIVEN - Campos gestionados por JPA
        ReflectionTestUtils.setField(notification, "id", 1L);

        ReflectionTestUtils.setField(notification, "createdAt",
                LocalDateTime.of(2026, 10, 20, 10, 0));

        // GIVEN - Campos editables
        notification.setType(NotificationType.FOLLOW_UP_DUE);
        notification.setTitle("Seguimiento pendiente");
        notification.setMessage("Contactar con el cliente");
        notification.setFollowUpId(10L);
        notification.setQuoteId(5L);
        notification.setRead(true);

        notification.setReadAt(
                LocalDateTime.of(2026, 10, 20, 11, 0));

        return notification;
    }

    // =====================================
    // 1. CONVERSIÓN COMPLETA
    // =====================================

    @Test
    void givenNotification_whenToDto_thenMapAllFields() {

        // GIVEN
        Notification notification = createNotification();

        // WHEN
        NotificationDto dto = mapper.toDto(notification);

        // THEN
        assertNotNull(dto);

        assertAll(
                () -> assertEquals(notification.getId(), dto.getId()),
                () -> assertEquals(notification.getType(), dto.getType()),
                () -> assertEquals(notification.getTitle(), dto.getTitle()),
                () -> assertEquals(notification.getMessage(), dto.getMessage()),
                () -> assertEquals(notification.getFollowUpId(), dto.getFollowUpId()),
                () -> assertEquals(notification.getQuoteId(), dto.getQuoteId()),
                () -> assertEquals(notification.getRead(), dto.getRead()),
                () -> assertEquals(notification.getReadAt(), dto.getReadAt()),
                () -> assertEquals(notification.getCreatedAt(), dto.getCreatedAt()));
    }

    // =====================================
    // 2. NOTIFICACIÓN NO LEÍDA
    // =====================================

    @Test
    void givenUnreadNotification_whenToDto_thenReadAtIsNull() {

        // GIVEN
        Notification notification = createNotification();

        notification.setRead(false);
        notification.setReadAt(null);

        // WHEN
        NotificationDto dto = mapper.toDto(notification);

        // THEN
        assertFalse(dto.getRead());
        assertNull(dto.getReadAt());
    }

    // =====================================
    // 3. CAMPOS OPCIONALES NULOS
    // =====================================

    @Test
    void givenNullOptionalFields_whenToDto_thenPreserveNulls() {

        // GIVEN
        Notification notification = createNotification();

        notification.setFollowUpId(null);
        notification.setQuoteId(null);
        notification.setReadAt(null);

        // WHEN
        NotificationDto dto = mapper.toDto(notification);

        // THEN
        assertAll(
                () -> assertNull(dto.getFollowUpId()),
                () -> assertNull(dto.getQuoteId()),
                () -> assertNull(dto.getReadAt()));
    }
}
