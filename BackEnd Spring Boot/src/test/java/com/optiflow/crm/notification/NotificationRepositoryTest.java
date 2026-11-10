package com.optiflow.crm.notification;

import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationType;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

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

    private User recipient;
    private User otherRecipient;

    private Long recipientId;
    private Long otherRecipientId;


    // =====================================================
    // CONFIGURACIÓN INICIAL
    // =====================================================

    @BeforeEach
    void setUp() {

        recipient = createUser("vendedor");
        otherRecipient = createUser("otro");

        entityManager.persist(recipient);
        entityManager.persist(otherRecipient);
        entityManager.flush();

        recipientId = recipient.getId();
        otherRecipientId = otherRecipient.getId();

        assertNotNull(recipientId);
        assertNotNull(otherRecipientId);
    }


    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    private User createUser(String prefix) {

        User user = new User();

        user.setFirstName("Usuario");
        user.setLastName("Prueba");

        user.setEmail(prefix + "-" + UUID.randomUUID()
                + "@optiflow.test");

        user.setPassword("password-test");
        user.setRole(UserRole.SELLER);
        user.setActive(true);

        return user;
    }

    private Notification createNotification(Long followUpId, NotificationType type) {

        return createNotificationFor(recipient, followUpId, type);
    }

    private Notification createNotificationFor(
            User user,
            Long followUpId,
            NotificationType type
    ) {

        Notification notification = new Notification();

        notification.setRecipient(user);

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

    private Notification persistNotificationFor(
            User user,
            Long followUpId,
            NotificationType type
    ) {

        return repository.saveAndFlush(createNotificationFor(user, followUpId, type));
    }

    private void flushAndClear() {

        entityManager.flush();
        entityManager.clear();
    }


    // =====================================================
    // 1. GUARDAR Y RECUPERAR
    // =====================================================

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
                () -> assertEquals(recipientId, found.getRecipient().getId()),
                () -> assertFalse(found.getRead()),
                () -> assertTrue(found.getActive()),
                () -> assertNull(found.getReadAt()),
                () -> assertNotNull(found.getCreatedAt())
        );
    }


    // =====================================================
    // 2. VALORES POR DEFECTO Y AUDITORÍA
    // =====================================================

    @Test
    void givenNullDefaults_whenSave_thenInitializeFields() {

        // GIVEN

        Notification notification = createNotification(
                11L,
                NotificationType.FOLLOW_UP_DUE
        );

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


    // =====================================================
    // 3. LISTAR SOLO ACTIVAS
    // =====================================================

    @Test
    void givenActiveAndInactive_whenFindActive_thenExcludeInactive() {

        // GIVEN

        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);
        persistNotification(11L, NotificationType.FOLLOW_UP_DUE);

        Notification inactive = persistNotification(12L, NotificationType.FOLLOW_UP_DUE);

        inactive.setActive(false);

        repository.saveAndFlush(inactive);

        flushAndClear();

        // WHEN

        Page<Notification> result = repository.findByRecipientIdAndActiveTrue(
                        recipientId,
                        PageRequest.of(0, 10)
                );

        // THEN

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(Notification::getActive));
        assertTrue(result.getContent().stream().noneMatch(n ->
                                Long.valueOf(12L).equals(n.getFollowUpId())));
        assertTrue(result.getContent().stream().allMatch(n ->
                                recipientId.equals(n.getRecipient().getId())));
    }


    // =====================================================
    // 4. PAGINACIÓN DE ACTIVAS
    // =====================================================

    @Test
    void givenThreeActive_whenFindActive_thenPaginate() {

        // GIVEN

        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);
        persistNotification(11L, NotificationType.FOLLOW_UP_DUE);
        persistNotification(12L, NotificationType.FOLLOW_UP_DUE);

        flushAndClear();

        // WHEN

        Page<Notification> result = repository.findByRecipientIdAndActiveTrue(
                        recipientId,
                        PageRequest.of(
                                0,
                                2,
                                Sort.by("followUpId").ascending()
                        ));

        // THEN

        assertAll(
                () -> assertEquals(3, result.getTotalElements()),
                () -> assertEquals(2, result.getContent().size()),
                () -> assertEquals(2, result.getTotalPages()),
                () -> assertEquals(10L, result.getContent().get(0).getFollowUpId()),
                () -> assertEquals(11L, result.getContent().get(1).getFollowUpId()));
    }


    // =====================================================
    // 5. SIN NOTIFICACIONES ACTIVAS
    // =====================================================

    @Test
    void givenOnlyInactive_whenFindActive_thenReturnEmpty() {

        // GIVEN

        Notification notification = persistNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        notification.setActive(false);

        repository.saveAndFlush(notification);

        flushAndClear();

        // WHEN

        Page<Notification> result = repository.findByRecipientIdAndActiveTrue(
                        recipientId,
                        PageRequest.of(0, 10));

        // THEN

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }


    // =====================================================
    // 6. LISTAR NO LEÍDAS
    // =====================================================

    @Test
    void givenUnreadNotifications_whenFindUnread_thenReturnPage() {

        // GIVEN

        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);
        persistNotification(11L, NotificationType.FOLLOW_UP_DUE);

        flushAndClear();

        // WHEN

        Page<Notification> result = repository.findByRecipientIdAndReadFalseAndActiveTrue(
                        recipientId,
                        PageRequest.of(0, 10));

        // THEN

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(n ->
                                !n.getRead() && n.getActive()));

        assertTrue(result.getContent().stream().allMatch(n ->
                                recipientId.equals(n.getRecipient().getId())));
    }


    // =====================================================
    // 7. NO LEÍDAS: EXCLUIR LEÍDAS E INACTIVAS
    // =====================================================

    @Test
    void givenMixedNotifications_whenFindUnread_thenFilterBothFlags() {

        // GIVEN

        persistNotification(10L, NotificationType.FOLLOW_UP_DUE);
        Notification read = persistNotification(11L,
                NotificationType.FOLLOW_UP_DUE);

        read.setRead(true);

        read.setReadAt(
                LocalDateTime.of(2026, 10, 20, 12, 0)
        );

        repository.saveAndFlush(read);

        Notification inactive = persistNotification(12L,
                NotificationType.FOLLOW_UP_DUE);

        inactive.setActive(false);

        repository.saveAndFlush(inactive);

        flushAndClear();

        // WHEN

        Page<Notification> result = repository.findByRecipientIdAndReadFalseAndActiveTrue(
                        recipientId, PageRequest.of(0, 10));

        // THEN

        assertEquals(1, result.getTotalElements());

        Notification found = result.getContent().getFirst();

        assertEquals(10L, found.getFollowUpId());

        assertFalse(found.getRead());
        assertTrue(found.getActive());
    }


    // =====================================================
    // 8. CONTAR NO LEÍDAS
    // =====================================================

    @Test
    void givenMixedNotifications_whenCountUnread_thenCountOnlyActiveUnread() {

        // GIVEN

        persistNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        persistNotification(
                11L,
                NotificationType.FOLLOW_UP_DUE
        );

        Notification read = persistNotification(
                12L,
                NotificationType.FOLLOW_UP_DUE
        );

        read.setRead(true);

        repository.saveAndFlush(read);

        Notification inactive = persistNotification(
                13L,
                NotificationType.FOLLOW_UP_DUE
        );

        inactive.setActive(false);

        repository.saveAndFlush(inactive);

        flushAndClear();

        // WHEN

        long result =
                repository.countByRecipientIdAndReadFalseAndActiveTrue(
                        recipientId
                );

        // THEN

        assertEquals(2L, result);
    }


    // =====================================================
    // 9. EXISTE NOTIFICACIÓN POR FOLLOWUP Y TIPO
    // =====================================================

    @Test
    void givenExistingFollowUpAndType_whenExists_thenReturnTrue() {

        // GIVEN

        persistNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        flushAndClear();

        // WHEN

        boolean exists =
                repository.existsByFollowUpIdAndType(
                        10L,
                        NotificationType.FOLLOW_UP_DUE
                );

        // THEN

        assertTrue(exists);
    }


    // =====================================================
    // 10. NO EXISTE TIPO DE NOTIFICACIÓN
    // =====================================================

    @Test
    void givenDifferentType_whenExists_thenReturnFalse() {

        // GIVEN

        persistNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        flushAndClear();

        // WHEN

        boolean exists =
                repository.existsByFollowUpIdAndType(
                        10L,
                        NotificationType.FOLLOW_UP_OVERDUE
                );

        // THEN

        assertFalse(exists);
    }


    // =====================================================
    // 11. RESTRICCIÓN UNIQUE
    // =====================================================

    @Test
    void givenDuplicateFollowUpAndType_whenSave_thenDatabaseRejectsIt() {

        // GIVEN

        persistNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        flushAndClear();

        Notification duplicate = createNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        // WHEN + THEN

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(duplicate)
        );
    }


    // =====================================================
    // 12. MISMO FOLLOWUP, TIPO DIFERENTE
    // =====================================================

    @Test
    void givenSameFollowUpDifferentType_whenSave_thenAllowBoth() {

        // GIVEN

        persistNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        // WHEN

        persistNotification(
                10L,
                NotificationType.FOLLOW_UP_OVERDUE
        );

        flushAndClear();

        // THEN

        assertEquals(
                2L,
                repository.count()
        );

        assertTrue(
                repository.existsByFollowUpIdAndType(
                        10L,
                        NotificationType.FOLLOW_UP_DUE
                )
        );

        assertTrue(
                repository.existsByFollowUpIdAndType(
                        10L,
                        NotificationType.FOLLOW_UP_OVERDUE
                )
        );
    }


    // =====================================================
    // 13. MARCAR COMO LEÍDA
    // =====================================================

    @Test
    void givenUnreadNotification_whenUpdateRead_thenPersistChanges() {

        // GIVEN

        Notification notification = persistNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        Long id = notification.getId();

        LocalDateTime readAt =
                LocalDateTime.of(2026, 10, 20, 12, 0);

        // WHEN

        notification.setRead(true);
        notification.setReadAt(readAt);

        repository.saveAndFlush(notification);

        flushAndClear();

        // THEN

        Notification updated = repository
                .findById(id)
                .orElseThrow();

        assertTrue(updated.getRead());

        assertEquals(
                readAt,
                updated.getReadAt()
        );

        assertEquals(
                0L,
                repository.countByRecipientIdAndReadFalseAndActiveTrue(
                        recipientId
                )
        );
    }


    // =====================================================
    // 14. DESACTIVACIÓN LÓGICA
    // =====================================================

    @Test
    void givenNotification_whenDeactivate_thenRemainInDatabase() {

        // GIVEN

        Notification notification = persistNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        Long id = notification.getId();

        // WHEN

        notification.setActive(false);

        repository.saveAndFlush(notification);

        flushAndClear();

        // THEN

        Notification found = repository
                .findById(id)
                .orElseThrow();

        assertFalse(found.getActive());

        assertTrue(
                repository.existsById(id)
        );

        assertEquals(
                0L,
                repository.findByRecipientIdAndActiveTrue(
                        recipientId,
                        PageRequest.of(0, 10)
                ).getTotalElements()
        );
    }


    // =====================================================
    // 15. CAMPOS OBLIGATORIOS
    // =====================================================

    @Test
    void givenNullTitle_whenSave_thenDatabaseRejectsIt() {

        // GIVEN

        Notification notification = createNotification(
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        notification.setTitle(null);

        // WHEN + THEN

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(notification)
        );
    }


    // =====================================================
    // 16. AISLAMIENTO ENTRE USUARIOS
    // =====================================================

    @Test
    void givenTwoRecipients_whenFindActive_thenReturnOnlyOwnNotifications() {

        // GIVEN

        persistNotificationFor(
                recipient,
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        persistNotificationFor(
                recipient,
                11L,
                NotificationType.FOLLOW_UP_DUE
        );

        persistNotificationFor(
                otherRecipient,
                12L,
                NotificationType.FOLLOW_UP_DUE
        );

        flushAndClear();

        // WHEN

        Page<Notification> firstUser =
                repository.findByRecipientIdAndActiveTrue(
                        recipientId,
                        PageRequest.of(0, 10)
                );

        Page<Notification> secondUser =
                repository.findByRecipientIdAndActiveTrue(
                        otherRecipientId,
                        PageRequest.of(0, 10)
                );

        // THEN

        assertEquals(
                2,
                firstUser.getTotalElements()
        );

        assertEquals(
                1,
                secondUser.getTotalElements()
        );

        assertTrue(
                firstUser.getContent().stream()
                        .allMatch(n ->
                                recipientId.equals(
                                        n.getRecipient().getId()
                                )
                        )
        );

        assertTrue(
                secondUser.getContent().stream()
                        .allMatch(n ->
                                otherRecipientId.equals(
                                        n.getRecipient().getId()
                                )
                        )
        );

        assertEquals(
                3L,
                repository.count()
        );
    }


    // =====================================================
    // 17. CONTADOR INDEPENDIENTE POR USUARIO
    // =====================================================

    @Test
    void givenTwoRecipients_whenCountUnread_thenReturnIndividualCounts() {

        // GIVEN

        persistNotificationFor(
                recipient,
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        persistNotificationFor(
                recipient,
                11L,
                NotificationType.FOLLOW_UP_DUE
        );

        persistNotificationFor(
                otherRecipient,
                12L,
                NotificationType.FOLLOW_UP_DUE
        );

        Notification read = persistNotificationFor(
                otherRecipient,
                13L,
                NotificationType.FOLLOW_UP_DUE
        );

        read.setRead(true);
        read.setReadAt(LocalDateTime.now());

        repository.saveAndFlush(read);

        flushAndClear();

        // WHEN

        long firstCount =
                repository.countByRecipientIdAndReadFalseAndActiveTrue(
                        recipientId
                );

        long secondCount =
                repository.countByRecipientIdAndReadFalseAndActiveTrue(
                        otherRecipientId
                );

        // THEN

        assertEquals(2L, firstCount);
        assertEquals(1L, secondCount);
    }


    // =====================================================
    // 18. ACCESO A NOTIFICACIÓN POR PROPIETARIO
    // =====================================================

    @Test
    void givenNotificationOfAnotherUser_whenFindById_thenDoNotExposeIt() {

        // GIVEN

        Notification notification = persistNotificationFor(
                recipient,
                10L,
                NotificationType.FOLLOW_UP_DUE
        );

        Long notificationId = notification.getId();

        flushAndClear();

        // WHEN + THEN

        assertTrue(
                repository.findByIdAndRecipientIdAndActiveTrue(
                        notificationId,
                        recipientId
                ).isPresent()
        );

        assertTrue(
                repository.findByIdAndRecipientIdAndActiveTrue(
                        notificationId,
                        otherRecipientId
                ).isEmpty()
        );

        assertEquals(
                recipientId,
                repository.findByIdAndRecipientIdAndActiveTrue(
                        notificationId,
                        recipientId
                ).orElseThrow().getRecipient().getId()
        );
    }
}
