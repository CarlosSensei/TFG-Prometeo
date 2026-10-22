package com.optiflow.crm.client.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas unitarias de las restricciones Bean Validation de ClientDto. */
class ClientDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void initializeValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        if (factory != null) factory.close();
    }

    private ClientDto validClient() {
        ClientDto dto = new ClientDto();
        dto.setName("Ana");
        dto.setSurname("García");
        dto.setLastSurname("López");
        dto.setBirthDate(LocalDate.of(1985, 5, 20));
        dto.setDni("12345678Z");
        dto.setEmail("ana@example.com");
        dto.setPhoneNumber("600111222");
        dto.setPostalCode("08036");
        return dto;
    }

    private Set<ConstraintViolation<ClientDto>> violations(ClientDto dto) {
        return validator.validate(dto);
    }

    private void assertViolationOn(ClientDto dto, String field) {

        Set<ConstraintViolation<ClientDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath()
                                .toString().equalsIgnoreCase(field)),
                "Se esperaba error en '" + field + "', pero se obtuvo: " + violations);
    }

    private void assertNoViolationOn(ClientDto dto, String field) {
        var errors = violations(dto);
        assertFalse(errors.stream().anyMatch(v -> field.equals(v.getPropertyPath().toString())),
                () -> "No se esperaba error en '" + field + "', pero se obtuvo: " + errors);
    }

    @Test
    void givenValidClient_whenValidate_thenNoViolations() {
        assertTrue(violations(validClient()).isEmpty(),
                () -> "El cliente base debería ser válido: " + violations(validClient()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sin-arroba", "ana@", "@example.com", "ana ejemplo@example.com"})
    void givenInvalidEmail_whenValidate_thenRejectEmail(String email) {
        ClientDto dto = validClient();
        dto.setEmail(email);
        assertViolationOn(dto, "email");
    }

    @Test
    void givenValidEmail_whenValidate_thenAcceptEmail() {
        assertNoViolationOn(validClient(), "email");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankName_whenValidate_thenRejectName(String name) {
        ClientDto dto = validClient();
        dto.setName(name);
        assertViolationOn(dto, "name");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "123456", "ABCDE", "12A45"})
    void givenInvalidPostalCode_whenValidate_thenRejectPostalCode(String postalCode) {
        ClientDto dto = validClient();
        dto.setPostalCode(postalCode);
        assertViolationOn(dto, "postalCode");
    }

    @ParameterizedTest
    @ValueSource(strings = {"08036", "28001", "41001"})
    void givenValidPostalCode_whenValidate_thenAcceptPostalCode(String postalCode) {
        ClientDto dto = validClient();
        dto.setPostalCode(postalCode);
        assertNoViolationOn(dto, "postalCode");
    }

    @Test
    void givenIncorrectDniCheckLetter_whenValidate_thenRejectDni() {

        ClientDto dto = validClient();
        dto.setDni("12345678A");

        Set<ConstraintViolation<ClientDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equalsIgnoreCase("dni")
                                && v.getMessage().equals("El DNI/NIE no es válido")));
    }

    @Test
    void givenValidDni_whenValidate_thenAcceptDni() {
        assertNoViolationOn(validClient(), "dni");
    }
}
