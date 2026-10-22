package com.optiflow.crm.user;

import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Cada test se ejecuta en una transacción
        // independiente que se revierte al finalizar.
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private User createUser(String email, boolean active) {

        User user = new User();

        user.setFirstName("Carlos");
        user.setLastName("García");
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(UserRole.SELLER);
        user.setActive(active);

        return user;
    }

    private User persistUser(String email, boolean active) {

        User user = createUser(email, active);

        return repository.saveAndFlush(user);
    }

    private void flushAndClear() {

        entityManager.flush();
        entityManager.clear();
    }

    // =====================================
    // 1. GUARDAR USUARIO
    // =====================================

    @Test
    void givenValidUser_whenSave_thenPersistAllFields() {

        // GIVEN
        User user = createUser("carlos@optiflow.com", true);

        // WHEN
        User saved = repository.saveAndFlush(user);

        Long id = saved.getId();

        flushAndClear();

        User found = repository.findById(id).orElseThrow();

        // THEN
        assertAll(
                () -> assertNotNull(found.getId()),
                () -> assertEquals("Carlos", found.getFirstName()),
                () -> assertEquals("García", found.getLastName()),
                () -> assertEquals("carlos@optiflow.com", found.getEmail()),
                () -> assertEquals("encoded-password", found.getPassword()),
                () -> assertEquals(UserRole.SELLER, found.getRole()),
                () -> assertTrue(found.getActive())
        );
    }

    // =====================================
    // 2. AUDITORÍA DE CREACIÓN
    // =====================================

    @Test
    void givenNewUser_whenSave_thenSetAuditDates() {

        // GIVEN
        User user = createUser("audit@optiflow.com", true);

        LocalDateTime before = LocalDateTime.now();

        // WHEN
        User saved = repository.saveAndFlush(user);

        LocalDateTime after = LocalDateTime.now();

        flushAndClear();

        User found = repository.findById(saved.getId()).orElseThrow();

        // THEN
        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getUpdatedAt());

        assertFalse(found.getCreatedAt().isBefore(before));
        assertFalse(found.getCreatedAt().isAfter(after));
        assertFalse(found.getUpdatedAt().isBefore(before));
    }

    // =====================================
    // 3. BÚSQUEDA POR EMAIL
    // =====================================

    @Test
    void givenExistingEmail_whenFindIgnoreCase_thenReturnUser() {

        // GIVEN
        User saved = persistUser("carlos@optiflow.com", true);

        flushAndClear();

        // WHEN
        Optional<User> result =
                repository.findByEmailIgnoreCase("CARLOS@OPTIFLOW.COM");

        // THEN
        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }

    // =====================================
    // 4. EMAIL INEXISTENTE
    // =====================================

    @Test
    void givenUnknownEmail_whenFindIgnoreCase_thenReturnEmpty() {

        // GIVEN
        persistUser("carlos@optiflow.com", true);

        flushAndClear();

        // WHEN
        Optional<User> result =
                repository.findByEmailIgnoreCase("unknown@optiflow.com");

        // THEN
        assertTrue(result.isEmpty());
    }

    // =====================================
    // 5. EXISTS BY EMAIL
    // =====================================

    @Test
    void givenExistingEmail_whenExistsIgnoreCase_thenReturnTrue() {

        // GIVEN
        persistUser("carlos@optiflow.com", true);

        flushAndClear();

        // WHEN
        boolean exists =
                repository.existsByEmailIgnoreCase("CARLOS@OPTIFLOW.COM");

        // THEN
        assertTrue(exists);
    }

    // =====================================
    // 6. EXISTS BY EMAIL INEXISTENTE
    // =====================================

    @Test
    void givenUnknownEmail_whenExistsIgnoreCase_thenReturnFalse() {

        // GIVEN
        persistUser("carlos@optiflow.com", true);

        flushAndClear();

        // WHEN
        boolean exists =
                repository.existsByEmailIgnoreCase("unknown@optiflow.com");

        // THEN
        assertFalse(exists);
    }

    // =====================================
    // 7. EMAIL DUPLICADO
    // =====================================

    @Test
    void givenDuplicateEmail_whenSave_thenDatabaseRejectsIt() {

        // GIVEN
        persistUser("duplicate@optiflow.com", true);

        flushAndClear();

        User duplicate = createUser("duplicate@optiflow.com", true);

        // WHEN + THEN
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(duplicate));
    }

    // =====================================
    // 8. FILTRAR USUARIOS ACTIVOS
    // =====================================

    @Test
    void givenActiveAndInactiveUsers_whenFindActive_thenExcludeInactive() {

        // GIVEN
        persistUser("active1@optiflow.com", true);
        persistUser("active2@optiflow.com", true);
        persistUser("inactive@optiflow.com", false);

        flushAndClear();

        // WHEN
        Page<User> result = repository.findByActiveTrue(
                        PageRequest.of(0, 10));

        // THEN
        assertEquals(2, result.getTotalElements());

        assertTrue(result.getContent()
                        .stream()
                        .allMatch(User::getActive));

        assertTrue(result.getContent()
                        .stream()
                        .noneMatch(user ->
                                "inactive@optiflow.com".equals(user.getEmail())));
    }

    // =====================================
    // 9. PAGINACIÓN
    // =====================================

    @Test
    void givenThreeActiveUsers_whenFindActive_thenPaginate() {

        // GIVEN
        persistUser("ana@optiflow.com", true);
        persistUser("bea@optiflow.com", true);
        persistUser("carlos@optiflow.com", true);
        persistUser("inactive@optiflow.com", false);

        flushAndClear();

        // WHEN
        Page<User> result = repository.findByActiveTrue(
                        PageRequest.of(0, 2,
                                Sort.by("email").ascending()));

        // THEN
        assertAll(
                () -> assertEquals(3, result.getTotalElements()),
                () -> assertEquals(2, result.getContent().size()),
                () -> assertEquals(2, result.getTotalPages()),
                () -> assertEquals("ana@optiflow.com",
                        result.getContent().get(0).getEmail()),
                () -> assertEquals("bea@optiflow.com",
                        result.getContent().get(1).getEmail()));
    }

    // =====================================
    // 10. SIN USUARIOS ACTIVOS
    // =====================================

    @Test
    void givenOnlyInactiveUsers_whenFindActive_thenReturnEmpty() {

        // GIVEN
        persistUser("inactive1@optiflow.com", false);
        persistUser("inactive2@optiflow.com", false);

        flushAndClear();

        // WHEN
        Page<User> result = repository.findByActiveTrue(
                        PageRequest.of(0, 10));

        // THEN
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // =====================================
    // 11. ACTUALIZAR USUARIO
    // =====================================

    @Test
    void givenExistingUser_whenUpdate_thenPersistChanges() {

        // GIVEN
        User user = persistUser("carlos@optiflow.com", true);

        Long id = user.getId();

        flushAndClear();

        // WHEN
        User existing = repository.findById(id).orElseThrow();

        existing.setFirstName("Carlos Alberto");
        existing.setRole(UserRole.OPTICIAN);

        repository.saveAndFlush(existing);

        flushAndClear();

        // THEN
        User updated = repository.findById(id).orElseThrow();

        assertEquals("Carlos Alberto", updated.getFirstName());
        assertEquals(UserRole.OPTICIAN, updated.getRole());
        assertNotNull(updated.getUpdatedAt());
    }

    // =====================================
    // 12. DESACTIVACIÓN LÓGICA
    // =====================================

    @Test
    void givenUser_whenDeactivate_thenRemainInDatabase() {

        // GIVEN
        User user = persistUser("inactive@optiflow.com", true);

        Long id = user.getId();

        // WHEN
        user.setActive(false);

        repository.saveAndFlush(user);

        flushAndClear();

        // THEN
        User found = repository.findById(id).orElseThrow();

        assertFalse(found.getActive());

        Page<User> activeUsers = repository.findByActiveTrue(
                        PageRequest.of(0, 10));

        assertEquals(0, activeUsers.getTotalElements());

        // El usuario continúa persistido.
        assertTrue(repository.existsById(id));
    }

    // =====================================
    // 13. ROL POR DEFECTO
    // =====================================

    @Test
    void givenNullRole_whenPersist_thenUseSellerDefault() {

        // GIVEN
        User user = createUser("default@optiflow.com", true);

        user.setRole(null);

        // WHEN
        User saved = repository.saveAndFlush(user);

        flushAndClear();

        // THEN
        User found = repository.findById(saved.getId()).orElseThrow();

        assertEquals(UserRole.SELLER, found.getRole());
    }

    // =====================================
    // 14. ELIMINACIÓN FÍSICA DEL REPOSITORIO
    // =====================================

    @Test
    void givenExistingUser_whenDeleteById_thenRemoveFromDatabase() {

        // GIVEN
        User user = persistUser("delete@optiflow.com", true);

        Long id = user.getId();

        flushAndClear();

        // WHEN
        repository.deleteById(id);
        repository.flush();

        entityManager.clear();

        // THEN
        assertFalse(repository.existsById(id));
        assertTrue(repository.findById(id).isEmpty());
    }
}
