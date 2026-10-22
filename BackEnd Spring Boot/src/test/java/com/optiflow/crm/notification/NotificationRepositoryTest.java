package com.optiflow.crm.notification;

import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationType;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private EntityManager entityManager;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Notification createNotification(Long followUpId, NotificationType type) {

        Notification notification = new Notification();

        notification.setFollowUpId(followUpId);
        notification.setQuoteId(5L);
        notification.setType(type);

        notification.setTitle("Seguimiento pendiente");
        notification.setMessage("Contactar con el cliente");

        notification.setRead(false);
        notification.setActive(true);

        return notification;
    }

    private Notification persistNotification(Long followUpId, NotificationType type) {

        return repository.saveAndFlush(createNotification(followUpId, type));
    }

    private void flushAndClear() {

        entityManager.flush();
        entityManager.clear();
    }

    // =====================================
    // 1. GUARDAR Y RECUPERAR
    // =====================================

    @Test
    void givenValidNotification_whenSave_thenPersistAllFields() {

        // GIVEN
        Notification notification = createNotification(10L,
                NotificationType.FOLLOW_UP_DUE);

        // WHEN
        Notification saved = repository.saveAndFlush(notification);
        Long id = saved.getId();

        flushAndClear();

        Notification found = repository.findById(id).orElseThrow();

        // THEN
        assertAll(
                () -> assertNotNull(found.getId()),
                () -> assertEquals(10L, found.getFollowUpId()),
                () -> assertEquals(5L, found.getQuoteId()),
                () -> assertEquals(NotificationType.FOLLOW_UP_DUE, found.getType()),
                () -> assertEquals("Seguimiento pendiente", found.getTitle()),
                () -> assertEquals("Contactar con el cliente", found.getMessage()),
                () -> assertFalse(found.getRead()),
                () -> assertTrue(found.getActive()),
                () -> assertNull(found.getReadAt()));
    }

    // =====================================
    // 2. VALORES POR DEFECTO Y AUDITORÍA
    // =====================================

    @Test
    void givenNullDefaults_whenSave_thenInitializeFields() {

        // GIVEN
        Notification notification = createNotification(11L,
                NotificationType.FOLLOW_UP_DUE);

        notification.setRead(null);
        notification.setActive(null);

        LocalDateTime before = LocalDateTime.now();

        // WHEN
        Notification saved = repository.saveAndFlush(notification);

        LocalDateTime after = LocalDateTime.now();
        Long id = saved.getId();

        flushAndClear();

        Notification found = repository.findById(id).orElseThrow();

        // THEN
        assertAll(
                () -> assertFalse(found.getRead()),
                () -> assertTrue(found.getActive()),
                () -> assertNotNull(found.getCreatedAt()),
                () -> assertFalse(found.getCreatedAt().isBefore(before)),
                () -> assertFalse(found.getCreatedAt().isAfter(after)));
    }

    // =====================================
    // 3. LISTAR SOLO ACTIVAS
    // =====================================

    @Test
    void givenActiveAndInactive_whenFindActive_thenExcludeInactive() {

        // GIVEN
        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);
        persistNotification(11L, NotificationType.FOLLOW_UP_DUE);

        Notification inactive = persistNotification(12L,NotificationType.FOLLOW_UP_DUE);

        inactive.setActive(false);
        repository.saveAndFlush(inactive);

        flushAndClear();

        // WHEN
        Page<Notification> result = repository.findByActiveTrue(PageRequest
                .of(0, 10));

        // THEN
        assertEquals(2, result.getTotalElements());

        assertTrue(result.getContent().stream().allMatch(Notification::getActive));

        assertTrue(
                result.getContent().stream().noneMatch(n ->
                        Long.valueOf(12L).equals(n.getFollowUpId())));
    }

    // =====================================
    // 4. PAGINACIÓN DE ACTIVAS
    // =====================================

    @Test
    void givenThreeActive_whenFindActive_thenPaginate() {

        // GIVEN
        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);
        persistNotification(11L, NotificationType.FOLLOW_UP_DUE);
        persistNotification(12L, NotificationType.FOLLOW_UP_DUE);

        flushAndClear();

        // WHEN
        Page<Notification> result = repository.findByActiveTrue(PageRequest
                .of(0, 2, Sort.by("followUpId").ascending()));

        // THEN
        assertAll(
                () -> assertEquals(3, result.getTotalElements()),
                () -> assertEquals(2, result.getContent().size()),
                () -> assertEquals(2, result.getTotalPages()),
                () -> assertEquals(10L, result.getContent().get(0).getFollowUpId()),
                () -> assertEquals(11L, result.getContent().get(1).getFollowUpId()));
    }

    // =====================================
    // 5. SIN NOTIFICACIONES ACTIVAS
    // =====================================

    @Test
    void givenOnlyInactive_whenFindActive_thenReturnEmpty() {

        // GIVEN
        Notification notification = persistNotification(10L,
                NotificationType.FOLLOW_UP_DUE);

        notification.setActive(false);
        repository.saveAndFlush(notification);

        flushAndClear();

        // WHEN
        Page<Notification> result = repository.findByActiveTrue(PageRequest
                .of(0, 10));

        // THEN
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // =====================================
    // 6. LISTAR NO LEÍDAS
    // =====================================

    @Test
    void givenUnreadNotifications_whenFindUnread_thenReturnPage() {

        // GIVEN
        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);
        persistNotification(11L, NotificationType.FOLLOW_UP_DUE);

        flushAndClear();

        // WHEN
        Page<Notification> result = repository.findByReadFalseAndActiveTrue(
                PageRequest.of(0, 10));

        // THEN
        assertEquals(2, result.getTotalElements());

        assertTrue(result.getContent().stream()
                .allMatch(n -> !n.getRead() && n.getActive()));
    }

    // =====================================
    // 7. NO LEÍDAS: EXCLUIR LEÍDAS E INACTIVAS
    // =====================================

    @Test
    void givenMixedNotifications_whenFindUnread_thenFilterBothFlags() {

        // GIVEN
        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);
        Notification read = persistNotification(11L, NotificationType.FOLLOW_UP_DUE);

        read.setRead(true);
        read.setReadAt(
                LocalDateTime.of(2026, 10, 20, 12, 0));

        repository.saveAndFlush(read);

        Notification inactive = persistNotification(12L,
                NotificationType.FOLLOW_UP_DUE);

        inactive.setActive(false);
        repository.saveAndFlush(inactive);

        flushAndClear();

        // WHEN
        Page<Notification> result = repository.findByReadFalseAndActiveTrue(
                PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());

        Notification found = result.getContent().getFirst();

        assertEquals(10L, found.getFollowUpId());
        assertFalse(found.getRead());
        assertTrue(found.getActive());
    }

    // =====================================
    // 8. CONTAR NO LEÍDAS
    // =====================================

    @Test
    void givenMixedNotifications_whenCountUnread_thenCountOnlyActiveUnread() {

        // GIVEN
        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);
        persistNotification(11L, NotificationType.FOLLOW_UP_DUE);

        Notification read = persistNotification(12L,
                NotificationType.FOLLOW_UP_DUE);

        read.setRead(true);
        repository.saveAndFlush(read);

        Notification inactive = persistNotification(13L,
                NotificationType.FOLLOW_UP_DUE);

        inactive.setActive(false);
        repository.saveAndFlush(inactive);

        flushAndClear();

        // WHEN
        long result = repository.countByReadFalseAndActiveTrue();

        // THEN
        assertEquals(2L, result);
    }

    // =====================================
    // 9. EXISTE NOTIFICACIÓN POR FOLLOWUP Y TIPO
    // =====================================

    @Test
    void givenExistingFollowUpAndType_whenExists_thenReturnTrue() {

        // GIVEN
        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);

        flushAndClear();

        // WHEN
        boolean exists = repository.existsByFollowUpIdAndType(10L,
                NotificationType.FOLLOW_UP_DUE);

        // THEN
        assertTrue(exists);
    }

    // =====================================
    // 10. NO EXISTE TIPO DE NOTIFICACIÓN
    // =====================================

    @Test
    void givenDifferentType_whenExists_thenReturnFalse() {

        // GIVEN
        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);

        flushAndClear();

        // WHEN
        boolean exists = repository.existsByFollowUpIdAndType(10L,
                NotificationType.FOLLOW_UP_OVERDUE);

        // THEN
        assertFalse(exists);
    }

    // =====================================
    // 11. RESTRICCIÓN UNIQUE
    // =====================================

    @Test
    void givenDuplicateFollowUpAndType_whenSave_thenDatabaseRejectsIt() {

        // GIVEN
        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);

        flushAndClear();

        Notification duplicate = createNotification(10L,
                NotificationType.FOLLOW_UP_DUE);

        // WHEN + THEN
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(duplicate));
    }

    // =====================================
    // 12. MISMO FOLLOWUP, TIPO DIFERENTE
    // =====================================

    @Test
    void givenSameFollowUpDifferentType_whenSave_thenAllowBoth() {

        // GIVEN
        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);

        // WHEN
        persistNotification(10L, NotificationType.FOLLOW_UP_OVERDUE);

        flushAndClear();

        // THEN
        assertEquals(2L, repository.count());
        assertTrue(repository.existsByFollowUpIdAndType(10L,
                NotificationType.FOLLOW_UP_DUE));
        assertTrue(repository.existsByFollowUpIdAndType(10L,
                NotificationType.FOLLOW_UP_OVERDUE));
    }

    // =====================================
    // 13. MARCAR COMO LEÍDA
    // =====================================

    @Test
    void givenUnreadNotification_whenUpdateRead_thenPersistChanges() {

        // GIVEN
        Notification notification = persistNotification(10L,
                NotificationType.FOLLOW_UP_DUE);

        Long id = notification.getId();

        LocalDateTime readAt =
                LocalDateTime.of(2026, 10, 20, 12, 0);

        // WHEN
        notification.setRead(true);
        notification.setReadAt(readAt);

        repository.saveAndFlush(notification);

        flushAndClear();

        // THEN
        Notification updated = repository.findById(id).orElseThrow();

        assertTrue(updated.getRead());
        assertEquals(readAt, updated.getReadAt());
        assertEquals(0L, repository.countByReadFalseAndActiveTrue());
    }

    // =====================================
    // 14. DESACTIVACIÓN LÓGICA
    // =====================================

    @Test
    void givenNotification_whenDeactivate_thenRemainInDatabase() {

        // GIVEN
        Notification notification = persistNotification(10L,
                NotificationType.FOLLOW_UP_DUE);

        Long id = notification.getId();

        // WHEN
        notification.setActive(false);
        repository.saveAndFlush(notification);

        flushAndClear();

        // THEN
        Notification found = repository.findById(id).orElseThrow();

        assertFalse(found.getActive());
        assertTrue(repository.existsById(id));

        assertEquals(0L, repository.findByActiveTrue(
                PageRequest.of(0, 10)).getTotalElements());
    }

    // =====================================
    // 15. CAMPOS OBLIGATORIOS
    // =====================================

    @Test
    void givenNullTitle_whenSave_thenDatabaseRejectsIt() {

        // GIVEN
        Notification notification = createNotification(10L,
                NotificationType.FOLLOW_UP_DUE);

        notification.setTitle(null);

        // WHEN + THEN
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(notification));
    }
}
