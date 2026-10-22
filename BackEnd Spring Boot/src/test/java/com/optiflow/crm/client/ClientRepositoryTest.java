package com.optiflow.crm.client;

import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.client.specification.ClientSpecification;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class ClientRepositoryTest {

    @Autowired
    private ClientRepository repository;

    @Autowired
    private EntityManager entityManager;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Client newClient(
            String name,
            String surname,
            String Dni,
            String phone,
            String city,
            boolean active) {

        Client client = new Client();

        client.setName(name);
        client.setSurname(surname);
        client.setDni(Dni);
        client.setPhoneNumber(phone);
        client.setCity(city);
        client.setActive(active);

        return client;
    }

    private Client createClient(
            String name,
            String surname,
            String Dni,
            String phone,
            String city,
            boolean active) {

        return repository.saveAndFlush(newClient(
                        name,
                        surname,
                        Dni,
                        phone,
                        city,
                        active
                )
        );
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Page<Client> search(ClientSearchDto dto) {

        return repository.findAll(ClientSpecification.filterBy(dto),
                PageRequest.of(0, 10, Sort.by("name")
                        .ascending()));
    }

    // =====================================
    // 1. SAVE AND FIND BY ID
    // =====================================

    @Test
    void givenValidClient_whenSave_thenPersistAndAudit() {

        // GIVEN
        Client client = createClient(
                "Ana",
                "García",
                "12345678Z",
                "600111222",
                "Barcelona",
                true
        );

        Long id = client.getId();

        flushAndClear();

        // WHEN
        Optional<Client> result = repository.findById(id);

        // THEN
        assertTrue(result.isPresent());

        Client found = result.get();

        assertAll(
                () -> assertEquals(id, found.getId()),
                () -> assertEquals("Ana", found.getName()),
                () -> assertEquals("García", found.getSurname()),
                () -> assertEquals("12345678Z", found.getDni()),
                () -> assertEquals("600111222", found.getPhoneNumber()),
                () -> assertEquals("Barcelona", found.getCity()),
                () -> assertTrue(found.getActive()),
                () -> assertNotNull(found.getCreatedAt()),
                () -> assertNotNull(found.getUpdatedAt())
        );
    }

    // =====================================
    // 2. FIND BY DNI IGNORE CASE
    // =====================================

    @Test
    void givenExistingDni_whenFindIgnoreCase_thenReturnClient() {

        // GIVEN
        createClient(
                "Ana",
                "García",
                "12345678Z",
                "600111222",
                "Barcelona",
                true
        );

        flushAndClear();

        // WHEN
        Optional<Client> result = repository.findByDniIgnoreCase("12345678z");

        // THEN
        assertTrue(result.isPresent());
        assertEquals("12345678Z", result.get().getDni());
    }

    // =====================================
    // 3. DNI NOT FOUND
    // =====================================

    @Test
    void givenUnknownDni_whenFind_thenReturnEmpty() {

        // WHEN
        Optional<Client> result = repository.findByDniIgnoreCase("99999999R");

        // THEN
        assertTrue(result.isEmpty());
    }

    // =====================================
    // 4. UNIQUE DNI
    // =====================================

    @Test
    void givenDuplicateDni_whenSave_thenDatabaseRejectsIt() {

        // GIVEN
        createClient(
                "Ana",
                "García",
                "12345678Z",
                "600111222",
                "Barcelona",
                true
        );

        Client duplicate = newClient(
                "Luis",
                "Martín",
                "12345678Z",
                "600333444",
                "Madrid",
                true
        );

        // WHEN + THEN
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(duplicate));

        // No realizar más consultas: la transacción
        // puede quedar invalidada tras el error.
    }

    // =====================================
    // 5. FIND ACTIVE
    // =====================================

    @Test
    void givenActiveAndInactiveClients_whenFindActive_thenExcludeInactive() {

        // GIVEN
        createClient(
                "Ana", "García", "12345678Z",
                "600111222", "Barcelona", true
        );

        createClient(
                "Luis", "Martín", "87654321X",
                "600333444", "Madrid", false
        );

        flushAndClear();

        // WHEN
        Page<Client> result = repository.findByActiveTrue(
                        PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("Ana", result.getContent().getFirst().getName());
        assertTrue(result.getContent().getFirst().getActive());
    }

    // =====================================
    // 6. PAGINATION
    // =====================================

    @Test
    void givenThreeClients_whenFindActive_thenPaginate() {

        // GIVEN
        createClient(
                "Ana", "García", "12345678Z",
                "600111222", "Barcelona", true
        );

        createClient(
                "Luis", "Martín", "87654321X",
                "600333444", "Madrid", true
        );

        createClient(
                "Marta", "López", "11223344B",
                "600555666", "Valencia", true
        );

        flushAndClear();

        // WHEN
        Page<Client> result = repository.findByActiveTrue(
                        PageRequest.of(0, 2,
                                Sort.by("name").ascending()));

        // THEN
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
        assertEquals(0, result.getNumber());

        assertEquals(List.of("Ana", "Luis"),
                result.getContent()
                        .stream()
                        .map(Client::getName)
                        .toList());
    }

    // =====================================
    // 7. SEARCH BY NAME AND CITY
    // =====================================

    @Test
    void givenNameAndCity_whenSearch_thenCombineFilters() {

        // GIVEN
        createClient(
                "Ana", "García", "12345678Z",
                "600111222", "Barcelona", true
        );

        createClient(
                "Ana", "Martín", "87654321X",
                "600333444", "Madrid", true
        );

        createClient(
                "Luis", "López", "11223344B",
                "600555666", "Barcelona", true
        );

        flushAndClear();

        ClientSearchDto searchDto = new ClientSearchDto();

        searchDto.setName("Ana");
        searchDto.setCity("BARCELONA");

        // WHEN
        Page<Client> result = search(searchDto);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("12345678Z", result.getContent().getFirst().getDni());
    }

    // =====================================
    // 8. SEARCH BY PARTIAL DNI
    // =====================================

    @Test
    void givenPartialDni_whenSearch_thenFindMatchingClient() {

        // GIVEN
        createClient(
                "Ana", "García", "12345678Z",
                "600111222", "Barcelona", true
        );

        createClient(
                "Luis", "Martín", "87654321X",
                "600333444", "Madrid", true
        );

        flushAndClear();

        ClientSearchDto searchDto = new ClientSearchDto();

        searchDto.setDni("3456");

        // WHEN
        Page<Client> result = search(searchDto);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("12345678Z", result.getContent().getFirst().getDni());
    }

    // =====================================
    // 9. SEARCH INACTIVE CLIENTS
    // =====================================

    @Test
    void givenActiveFalse_whenSearch_thenReturnInactiveClients() {

        // GIVEN
        createClient(
                "Ana", "García", "12345678Z",
                "600111222", "Barcelona", true
        );

        createClient(
                "Luis", "Martín", "87654321X",
                "600333444", "Madrid", false
        );

        flushAndClear();

        ClientSearchDto searchDto = new ClientSearchDto();

        searchDto.setActive(false);

        // WHEN
        Page<Client> result = search(searchDto);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("87654321X", result.getContent().getFirst().getDni());
    }

    // =====================================
    // 10. UPDATE CLIENT
    // =====================================

    @Test
    void givenExistingClient_whenUpdate_thenPersistChanges() {

        // GIVEN
        Client client = createClient(
                "Ana", "García", "12345678Z",
                "600111222", "Barcelona", true
        );

        Long id = client.getId();

        // WHEN
        client.setCity("Girona");
        client.setPhoneNumber("600999888");

        repository.saveAndFlush(client);

        flushAndClear();

        // THEN
        Client updated = repository.findById(id).orElseThrow();

        assertAll(
                () -> assertEquals("Girona", updated.getCity()),
                () -> assertEquals("600999888", updated.getPhoneNumber()),
                () -> assertNotNull(updated.getUpdatedAt()));
    }

    // =====================================
    // 11. LOGICAL DELETE
    // =====================================

    @Test
    void givenClient_whenDeactivate_thenRemainInDatabase() {

        // GIVEN
        Client client = createClient(
                "Ana", "García", "12345678Z",
                "600111222", "Barcelona", true
        );

        Long id = client.getId();

        // WHEN
        client.setActive(false);

        repository.saveAndFlush(client);

        flushAndClear();

        // THEN
        Client found = repository.findById(id).orElseThrow();

        assertFalse(found.getActive());

        assertEquals(0, repository.findByActiveTrue(PageRequest
                .of(0, 10)).getTotalElements());
    }

    // =====================================
    // 12. PHYSICAL DELETE
    // =====================================

    @Test
    void givenExistingClient_whenDelete_thenRemoveFromDatabase() {

        // GIVEN
        Client client = createClient(
                "Ana", "García", "12345678Z",
                "600111222", "Barcelona", true
        );

        Long id = client.getId();

        // WHEN
        repository.delete(client);

        flushAndClear();

        // THEN
        assertFalse(repository.existsById(id));
    }
}
