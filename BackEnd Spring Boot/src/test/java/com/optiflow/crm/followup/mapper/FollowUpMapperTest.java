package com.optiflow.crm.followup.mapper;

import com.optiflow.crm.followup.dto.FollowUpDto;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;
import com.optiflow.crm.quote.model.Quote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FollowUpMapperTest {

    private FollowUpMapper mapper;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {
        mapper = new FollowUpMapper();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private FollowUp completeFollowUp() {

        Quote quote = new Quote();
        quote.setId(5L);

        FollowUp followUp = new FollowUp();

        followUp.setId(10L);
        followUp.setQuote(quote);
        followUp.setType(FollowUpType.CALL);
        followUp.setStatus(FollowUpStatus.COMPLETED);

        followUp.setScheduledAt(LocalDateTime
                .of(2026, 10, 25, 10, 0));
        followUp.setCompletedAt(LocalDateTime
                .of(2026, 10, 25, 11, 0));
        followUp.setNotes("Llamar al cliente");
        followUp.setResult("Cliente interesado");
        followUp.setAutomated(false);
        followUp.setActive(true);
        followUp.setCreatedAt(LocalDateTime
                .of(2026, 10, 20, 9, 0));
        followUp.setUpdatedAt(LocalDateTime
                .of(2026, 10, 25, 11, 0));

        return followUp;
    }

    private FollowUpDto completeDto() {

        FollowUpDto dto = new FollowUpDto();

        dto.setId(99L);
        dto.setQuoteId(7L);
        dto.setType(FollowUpType.EMAIL);
        dto.setStatus(FollowUpStatus.COMPLETED);
        dto.setScheduledAt(LocalDateTime
                .of(2026, 11, 1, 10, 0));
        dto.setCompletedAt(LocalDateTime
                .of(2026, 11, 1, 12, 0));
        dto.setNotes("Enviar correo");
        dto.setResult("Cliente contactado");
        dto.setAutomated(true);
        dto.setActive(false);
        dto.setCreatedAt(LocalDateTime
                .of(2026, 9, 1, 9, 0));
        dto.setUpdatedAt(LocalDateTime
                .of(2026, 9, 5, 15, 0));

        return dto;
    }

    // =====================================
    // 1. TO DTO - CONVERSIÓN COMPLETA
    // =====================================

    @Test
    void givenCompleteFollowUp_whenToDto_thenMapAllFields() {

        // GIVEN
        FollowUp followUp = completeFollowUp();

        // WHEN
        FollowUpDto dto = mapper.toDto(followUp);

        // THEN
        assertNotNull(dto);

        assertAll("FollowUp -> FollowUpDto",
                () -> assertEquals(followUp.getId(), dto.getId()),
                () -> assertEquals(followUp.getQuote().getId(), dto.getQuoteId()),
                () -> assertEquals(followUp.getType(), dto.getType()),
                () -> assertEquals(followUp.getStatus(), dto.getStatus()),
                () -> assertEquals(followUp.getScheduledAt(), dto.getScheduledAt()),
                () -> assertEquals(followUp.getCompletedAt(), dto.getCompletedAt()),
                () -> assertEquals(followUp.getNotes(), dto.getNotes()),
                () -> assertEquals(followUp.getResult(), dto.getResult()),
                () -> assertEquals(followUp.getAutomated(), dto.getAutomated()),
                () -> assertEquals(followUp.getActive(), dto.getActive()),
                () -> assertEquals(followUp.getCreatedAt(), dto.getCreatedAt()),
                () -> assertEquals(followUp.getUpdatedAt(), dto.getUpdatedAt()));
    }

    // =====================================
    // 2. TO DTO - PRESUPUESTO NULO
    // =====================================

    @Test
    void givenNullQuote_whenToDto_thenQuoteIdIsNull() {

        // GIVEN
        FollowUp followUp = completeFollowUp();
        followUp.setQuote(null);

        // WHEN
        FollowUpDto dto = mapper.toDto(followUp);

        // THEN
        assertNull(dto.getQuoteId());
        assertEquals(10L, dto.getId());
    }

    // =====================================
    // 3. TO DTO - CAMPOS OPCIONALES NULOS
    // =====================================

    @Test
    void givenNullOptionalFields_whenToDto_thenPreserveNulls() {

        // GIVEN
        FollowUp followUp = completeFollowUp();

        followUp.setCompletedAt(null);
        followUp.setNotes(null);
        followUp.setResult(null);
        followUp.setCreatedAt(null);
        followUp.setUpdatedAt(null);

        // WHEN
        FollowUpDto dto = mapper.toDto(followUp);

        // THEN
        assertAll(
                () -> assertNull(dto.getCompletedAt()),
                () -> assertNull(dto.getNotes()),
                () -> assertNull(dto.getResult()),
                () -> assertNull(dto.getCreatedAt()),
                () -> assertNull(dto.getUpdatedAt())
        );
    }

    // =====================================
    // 4. TO ENTITY - CONVERSIÓN COMPLETA
    // =====================================

    @Test
    void givenCompleteDto_whenToEntity_thenMapEditableFields() {

        // GIVEN
        FollowUpDto dto = completeDto();

        // WHEN
        FollowUp followUp = mapper.toEntity(dto);

        // THEN
        assertNotNull(followUp);

        assertAll("FollowUpDto -> FollowUp",
                () -> assertEquals(dto.getType(), followUp.getType()),
                () -> assertEquals(dto.getStatus(), followUp.getStatus()),
                () -> assertEquals(dto.getScheduledAt(), followUp.getScheduledAt()),
                () -> assertEquals(dto.getCompletedAt(), followUp.getCompletedAt()),
                () -> assertEquals(dto.getNotes(), followUp.getNotes()),
                () -> assertEquals(dto.getResult(), followUp.getResult()),
                () -> assertEquals(dto.getAutomated(), followUp.getAutomated()));
    }

    // =====================================
    // 5. TO ENTITY - CAMPOS PROTEGIDOS
    // =====================================

    @Test
    void givenDtoWithControlledFields_whenToEntity_thenIgnoreThem() {

        // GIVEN
        FollowUpDto dto = completeDto();

        // WHEN
        FollowUp followUp = mapper.toEntity(dto);

        // THEN
        assertNull(followUp.getId());
        assertNull(followUp.getQuote());

        // Valor predeterminado de la entidad.
        assertTrue(followUp.getActive());

        // La auditoría se genera mediante JPA.
        assertNull(followUp.getCreatedAt());
        assertNull(followUp.getUpdatedAt());

        assertNotEquals(dto.getActive(), followUp.getActive());
    }

    // =====================================
    // 6. TO ENTITY - TIPO WHATSAPP
    // =====================================

    @Test
    void givenWhatsappType_whenToEntity_thenPreserveType() {

        // GIVEN
        FollowUpDto dto = completeDto();

        dto.setType(FollowUpType.WHATSAPP);

        // WHEN
        FollowUp followUp = mapper.toEntity(dto);

        // THEN
        assertEquals(FollowUpType.WHATSAPP, followUp.getType());
    }

    // =====================================
    // 7. UPDATE - CAMPOS EDITABLES
    // =====================================

    @Test
    void givenDto_whenUpdateEntity_thenUpdateEditableFields() {

        // GIVEN
        FollowUp existing = completeFollowUp();
        FollowUpDto dto = completeDto();

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertAll("Campos editables",
                () -> assertEquals(dto.getType(), existing.getType()),
                () -> assertEquals(dto.getStatus(), existing.getStatus()),
                () -> assertEquals(dto.getScheduledAt(), existing.getScheduledAt()),
                () -> assertEquals(dto.getCompletedAt(), existing.getCompletedAt()),
                () -> assertEquals(dto.getNotes(), existing.getNotes()),
                () -> assertEquals(dto.getResult(), existing.getResult()),
                () -> assertEquals(dto.getAutomated(), existing.getAutomated()));
    }

    // =====================================
    // 8. UPDATE - CAMPOS PROTEGIDOS
    // =====================================

    @Test
    void givenDto_whenUpdateEntity_thenPreserveControlledFields() {

        // GIVEN
        FollowUp existing = completeFollowUp();
        FollowUpDto dto = completeDto();

        Long originalId = existing.getId();
        Quote originalQuote = existing.getQuote();
        Boolean originalActive = existing.getActive();

        LocalDateTime originalCreatedAt = existing.getCreatedAt();
        LocalDateTime originalUpdatedAt = existing.getUpdatedAt();

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertAll("Campos protegidos",
                () -> assertEquals(originalId, existing.getId()),
                () -> assertSame(originalQuote, existing.getQuote()),
                () -> assertEquals(originalActive, existing.getActive()),
                () -> assertEquals(originalCreatedAt, existing.getCreatedAt()),
                () -> assertEquals(originalUpdatedAt, existing.getUpdatedAt()));
    }

    // =====================================
    // 9. UPDATE - VALORES NULOS
    // =====================================

    @Test
    void givenNullFields_whenUpdateEntity_thenOverwriteWithNull() {

        // GIVEN
        FollowUp existing = completeFollowUp();

        FollowUpDto dto = completeDto();

        dto.setNotes(null);
        dto.setResult(null);
        dto.setCompletedAt(null);

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertNull(existing.getNotes());
        assertNull(existing.getResult());
        assertNull(existing.getCompletedAt());
    }

    // =====================================
    // 10. TO ENTITY - VALORES POR DEFECTO
    // =====================================

    @Test
    void givenNullStatusAndAutomated_whenToEntity_thenKeepNulls() {

        // GIVEN
        FollowUpDto dto = completeDto();

        dto.setStatus(null);
        dto.setAutomated(null);

        // WHEN
        FollowUp followUp = mapper.toEntity(dto);

        // THEN
        assertNull(followUp.getStatus());
        assertNull(followUp.getAutomated());

        // El mapper no modifica el estado activo.
        assertTrue(followUp.getActive());

        // Los valores por defecto se aplican en
        // el servicio o durante @PrePersist.
    }
}
