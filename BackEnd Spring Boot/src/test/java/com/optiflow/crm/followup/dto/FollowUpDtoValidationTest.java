package com.optiflow.crm.followup.dto;

import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FollowUpDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private FollowUpDto validFollowUp() {

        FollowUpDto dto = new FollowUpDto();

        dto.setQuoteId(5L);
        dto.setType(FollowUpType.CALL);
        dto.setStatus(FollowUpStatus.PENDING);
        dto.setScheduledAt(LocalDateTime
                .of(2026, 11, 10, 10, 0));
        dto.setNotes("Llamar al cliente");
        dto.setResult(null);
        dto.setAutomated(false);
        dto.setActive(true);

        return dto;
    }

    private CompleteFollowUpDto validCompleteDto() {

        CompleteFollowUpDto dto = new CompleteFollowUpDto();

        dto.setResult("Cliente interesado");
        dto.setNotes("Solicita información adicional");

        return dto;
    }

    private void assertViolationOn(Object dto, String field) {

        Set<ConstraintViolation<Object>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath()
                        .toString().equals(field)),
                "Se esperaba una violación en '" + field +
                        "', pero se obtuvo: " + violations);
    }

    private void assertValid(Object dto) {

        Set<ConstraintViolation<Object>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "El DTO debería ser válido: " + violations);
    }

    // =====================================
    // 1. FOLLOWUP VÁLIDO
    // =====================================

    @Test
    void givenValidFollowUp_whenValidate_thenNoViolations() {

        // GIVEN
        FollowUpDto dto = validFollowUp();

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 2. QUOTE ID OBLIGATORIO
    // =====================================

    @Test
    void givenNullQuoteId_whenValidate_thenReject() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setQuoteId(null);

        // WHEN + THEN
        assertViolationOn(dto, "quoteId");
    }

    // =====================================
    // 3. QUOTE ID POSITIVO
    // =====================================

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void givenInvalidQuoteId_whenValidate_thenReject(long quoteId) {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setQuoteId(quoteId);

        // WHEN + THEN
        assertViolationOn(dto, "quoteId");
    }

    @Test
    void givenPositiveQuoteId_whenValidate_thenAccept() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setQuoteId(1L);

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 4. TYPE OBLIGATORIO
    // =====================================

    @Test
    void givenNullType_whenValidate_thenReject() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setType(null);

        // WHEN + THEN
        assertViolationOn(dto, "type");
    }

    @Test
    void givenWhatsappType_whenValidate_thenAccept() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setType(FollowUpType.WHATSAPP);

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 5. FECHA PROGRAMADA
    // =====================================

    @Test
    void givenNullScheduledAt_whenValidate_thenReject() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setScheduledAt(null);

        // WHEN + THEN
        assertViolationOn(dto, "scheduledAt");
    }

    @Test
    void givenPastScheduledAt_whenValidate_thenAccept() {

        // GIVEN
        FollowUpDto dto = validFollowUp();

        dto.setScheduledAt(LocalDateTime
                .of(2020, 1, 1, 10, 0));

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 6. NOTES - NULL
    // =====================================

    @Test
    void givenNullNotes_whenValidate_thenAccept() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setNotes(null);

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 7. NOTES - LONGITUD
    // =====================================

    @Test
    void givenNotesLongerThan1000_whenValidate_thenReject() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setNotes("A".repeat(1001));

        // WHEN + THEN
        assertViolationOn(dto, "notes");
    }

    @Test
    void givenNotesWith1000Characters_whenValidate_thenAccept() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setNotes("A".repeat(1000));

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 8. RESULT - NULL
    // =====================================

    @Test
    void givenNullResult_whenValidate_thenAccept() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setResult(null);

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 9. RESULT - LONGITUD
    // =====================================

    @Test
    void givenResultLongerThan500_whenValidate_thenReject() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setResult("A".repeat(501));

        // WHEN + THEN
        assertViolationOn(dto, "result");
    }

    @Test
    void givenResultWith500Characters_whenValidate_thenAccept() {

        // GIVEN
        FollowUpDto dto = validFollowUp();
        dto.setResult("A".repeat(500));

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 10. CAMPOS OPCIONALES
    // =====================================

    @Test
    void givenNullOptionalFields_whenValidate_thenAccept() {

        // GIVEN
        FollowUpDto dto = validFollowUp();

        dto.setId(null);
        dto.setStatus(null);
        dto.setCompletedAt(null);
        dto.setAutomated(null);
        dto.setActive(null);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 11. COMPLETE DTO VÁLIDO
    // =====================================

    @Test
    void givenValidCompleteDto_whenValidate_thenNoViolations() {

        // GIVEN
        CompleteFollowUpDto dto = validCompleteDto();

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 12. COMPLETE - RESULT NULL
    // =====================================

    @Test
    void givenNullResult_whenValidateComplete_thenAccept() {

        // GIVEN
        CompleteFollowUpDto dto = validCompleteDto();
        dto.setResult(null);

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 13. COMPLETE - RESULT LONGITUD
    // =====================================

    @Test
    void givenResultLongerThan500_whenValidateComplete_thenReject() {

        // GIVEN
        CompleteFollowUpDto dto = validCompleteDto();
        dto.setResult("A".repeat(501));

        // WHEN + THEN
        assertViolationOn(dto, "result");
    }

    @Test
    void givenResultWith500Characters_whenValidateComplete_thenAccept() {

        // GIVEN
        CompleteFollowUpDto dto = validCompleteDto();
        dto.setResult("A".repeat(500));

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 14. COMPLETE - NOTES NULL
    // =====================================

    @Test
    void givenNullNotes_whenValidateComplete_thenAccept() {

        // GIVEN
        CompleteFollowUpDto dto = validCompleteDto();
        dto.setNotes(null);

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 15. COMPLETE - NOTES LONGITUD
    // =====================================

    @Test
    void givenNotesLongerThan1000_whenValidateComplete_thenReject() {

        // GIVEN
        CompleteFollowUpDto dto = validCompleteDto();
        dto.setNotes("A".repeat(1001));

        // WHEN + THEN
        assertViolationOn(dto, "notes");
    }

    @Test
    void givenNotesWith1000Characters_whenValidateComplete_thenAccept() {

        // GIVEN
        CompleteFollowUpDto dto = validCompleteDto();
        dto.setNotes("A".repeat(1000));

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 16. COMPLETE DTO VACÍO
    // =====================================

    @Test
    void givenEmptyCompleteDto_whenValidate_thenAccept() {

        // GIVEN
        CompleteFollowUpDto dto = new CompleteFollowUpDto();

        // WHEN + THEN
        assertValid(dto);
    }
}
