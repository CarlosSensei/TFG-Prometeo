package com.optiflow.crm.client.mapper;

import com.optiflow.crm.client.dto.ClientDto;
import com.optiflow.crm.client.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClientMapperTest {

    private ClientMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ClientMapper();
    }

    @Test
    void givenCompleteClient_whenToDto_thenCopyAllFields() {
        // GIVEN
        Client client = completeClient();

        // WHEN
        ClientDto dto = mapper.toDto(client);

        // THEN
        assertAll("Client -> ClientDto",
                () -> assertEquals(client.getId(), dto.getId()),
                () -> assertEquals(client.getName(), dto.getName()),
                () -> assertEquals(client.getSurname(), dto.getSurname()),
                () -> assertEquals(client.getLastSurname(), dto.getLastSurname()),
                () -> assertEquals(client.getDni(), dto.getDni()),
                () -> assertEquals(client.getPhoneNumber(), dto.getPhoneNumber()),
                () -> assertEquals(client.getEmail(), dto.getEmail()),
                () -> assertEquals(client.getBirthDate(), dto.getBirthDate()),
                () -> assertEquals(client.getAddress(), dto.getAddress()),
                () -> assertEquals(client.getCity(), dto.getCity()),
                () -> assertEquals(client.getPostalCode(), dto.getPostalCode()),
                () -> assertEquals(client.getNotes(), dto.getNotes(), "toDto debe copiar notes"),
                () -> assertEquals(client.getActive(), dto.getActive()),
                () -> assertEquals(client.getCreatedAt(), dto.getCreatedAt()),
                () -> assertEquals(client.getUpdatedAt(), dto.getUpdatedAt())
        );
    }

    @Test
    void givenNullOptionalFields_whenToDto_thenPreserveNulls() {
        Client client = new Client();
        client.setId(3L);
        client.setName("Eva");
        client.setSurname("López");
        client.setLastSurname(null);
        client.setEmail(null);
        client.setAddress(null);
        client.setCity(null);
        client.setPostalCode(null);
        client.setNotes(null);

        ClientDto dto = mapper.toDto(client);

        assertAll("Campos opcionales nulos",
                () -> assertEquals(3L, dto.getId()),
                () -> assertNull(dto.getLastSurname()),
                () -> assertNull(dto.getEmail()),
                () -> assertNull(dto.getAddress()),
                () -> assertNull(dto.getCity()),
                () -> assertNull(dto.getPostalCode()),
                () -> assertNull(dto.getNotes())
        );
    }

    @Test
    void givenCompleteDto_whenToEntity_thenCopyEditableFields() {
        ClientDto dto = completeDto();

        Client client = mapper.toEntity(dto);

        assertAll("ClientDto -> Client",
                () -> assertEquals(dto.getName(), client.getName()),
                () -> assertEquals(dto.getSurname(), client.getSurname()),
                () -> assertEquals(dto.getLastSurname(), client.getLastSurname()),
                () -> assertEquals(dto.getDni(), client.getDni()),
                () -> assertEquals(dto.getPhoneNumber(), client.getPhoneNumber()),
                () -> assertEquals(dto.getEmail(), client.getEmail()),
                () -> assertEquals(dto.getBirthDate(), client.getBirthDate()),
                () -> assertEquals(dto.getAddress(), client.getAddress()),
                () -> assertEquals(dto.getCity(), client.getCity()),
                () -> assertEquals(dto.getPostalCode(), client.getPostalCode()),
                () -> assertEquals(dto.getNotes(), client.getNotes(), "toEntity debe copiar notes")
        );
    }

    @Test
    void givenDtoWithServerFields_whenToEntity_thenDoNotCopyIdStatusOrAuditDates() {
        ClientDto dto = completeDto();
        dto.setId(99L);
        dto.setActive(false);
        dto.setCreatedAt(LocalDateTime.of(2020, 1, 1, 8, 0));
        dto.setUpdatedAt(LocalDateTime.of(2020, 1, 2, 8, 0));

        Client client = mapper.toEntity(dto);

        assertAll("Campos gestionados por la aplicación / JPA",
                () -> assertNull(client.getId()),
                () -> assertEquals(Boolean.TRUE, client.getActive()),
                () -> assertNull(client.getCreatedAt()),
                () -> assertNull(client.getUpdatedAt())
        );
    }

    @Test
    void givenChanges_whenUpdateEntity_thenUpdateEditableFieldsAndKeepSystemFields() {
        Client existing = completeClient();
        Long originalId = existing.getId();
        Boolean originalActive = existing.getActive();
        LocalDateTime originalCreatedAt = existing.getCreatedAt();
        LocalDateTime originalUpdatedAt = existing.getUpdatedAt();

        ClientDto changes = completeDto();
        changes.setId(999L);
        changes.setActive(false);
        changes.setCreatedAt(LocalDateTime.of(2010, 1, 1, 0, 0));
        changes.setUpdatedAt(LocalDateTime.of(2010, 1, 2, 0, 0));

        mapper.updateEntity(changes, existing);

        assertAll("Actualización de campos editables",
                () -> assertEquals(changes.getName(), existing.getName()),
                () -> assertEquals(changes.getSurname(), existing.getSurname()),
                () -> assertEquals(changes.getLastSurname(), existing.getLastSurname()),
                () -> assertEquals(changes.getDni(), existing.getDni()),
                () -> assertEquals(changes.getPhoneNumber(), existing.getPhoneNumber()),
                () -> assertEquals(changes.getEmail(), existing.getEmail()),
                () -> assertEquals(changes.getBirthDate(), existing.getBirthDate()),
                () -> assertEquals(changes.getAddress(), existing.getAddress()),
                () -> assertEquals(changes.getCity(), existing.getCity()),
                () -> assertEquals(changes.getPostalCode(), existing.getPostalCode()),
                () -> assertEquals(changes.getNotes(), existing.getNotes())
        );
        assertAll("Metadatos conservados",
                () -> assertEquals(originalId, existing.getId()),
                () -> assertEquals(originalActive, existing.getActive()),
                () -> assertEquals(originalCreatedAt, existing.getCreatedAt()),
                () -> assertEquals(originalUpdatedAt, existing.getUpdatedAt())
        );
    }

    @Test
    void givenNullOptionalValues_whenUpdateEntity_thenOverwriteWithNulls() {
        Client existing = completeClient();
        ClientDto changes = completeDto();
        changes.setLastSurname(null);
        changes.setEmail(null);
        changes.setAddress(null);
        changes.setCity(null);
        changes.setPostalCode(null);
        changes.setNotes(null);

        mapper.updateEntity(changes, existing);

        assertAll("El mapper actual permite limpiar campos opcionales con null",
                () -> assertNull(existing.getLastSurname()),
                () -> assertNull(existing.getEmail()),
                () -> assertNull(existing.getAddress()),
                () -> assertNull(existing.getCity()),
                () -> assertNull(existing.getPostalCode()),
                () -> assertNull(existing.getNotes())
        );
    }

    private static Client completeClient() {
        Client client = new Client();
        client.setId(1L);
        client.setName("Javier");
        client.setSurname("García");
        client.setLastSurname("López");
        client.setDni("12345678Z");
        client.setPhoneNumber("600111222");
        client.setEmail("javier@example.com");
        client.setBirthDate(LocalDate.of(1982, 3, 15));
        client.setAddress("Carrer Mallorca 120");
        client.setCity("Barcelona");
        client.setPostalCode("08036");
        client.setNotes("Usa lentes progresivas");
        client.setActive(true);
        client.setCreatedAt(LocalDateTime.of(2026, 10, 1, 9, 30));
        client.setUpdatedAt(LocalDateTime.of(2026, 10, 2, 10, 15));
        return client;
    }

    private static ClientDto completeDto() {
        ClientDto dto = new ClientDto();
        dto.setName("Ana");
        dto.setSurname("Soler");
        dto.setLastSurname("Martín");
        dto.setDni("87654321X");
        dto.setPhoneNumber("611222333");
        dto.setEmail("ana@example.com");
        dto.setBirthDate(LocalDate.of(1990, 4, 20));
        dto.setAddress("Carrer Aragó 50");
        dto.setCity("Barcelona");
        dto.setPostalCode("08015");
        dto.setNotes("Prefiere contacto por email");
        return dto;
    }
}
