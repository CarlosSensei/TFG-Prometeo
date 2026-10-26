package com.optiflow.crm.auth.dto;

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

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestValidationTest {

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

    private LoginRequest validRequest() {

        LoginRequest request = new LoginRequest();

        request.setEmail("carlos@optiflow.com");
        request.setPassword("Password123!");

        return request;
    }

    private Set<ConstraintViolation<LoginRequest>> validate(LoginRequest request) {

        return validator.validate(request);
    }

    private void assertValid(LoginRequest request) {

        Set<ConstraintViolation<LoginRequest>> violations = validate(request);

        assertTrue(violations.isEmpty(), "La petición debería ser válida: " + violations);
    }

    private void assertViolationOn(LoginRequest request, String field) {

        Set<ConstraintViolation<LoginRequest>> violations = validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals(field)),
                "Se esperaba una violación en '" + field +
                        "', pero se obtuvo: " + violations);
    }

    // =====================================
    // 1. LOGIN VÁLIDO
    // =====================================

    @Test
    void givenValidCredentials_whenValidate_thenNoViolations() {

        // GIVEN
        LoginRequest request = validRequest();

        // WHEN + THEN
        assertValid(request);
    }

    // =====================================
    // 2. EMAIL NULO O VACÍO
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankEmail_whenValidate_thenReject(String email) {

        // GIVEN
        LoginRequest request = validRequest();
        request.setEmail(email);

        // WHEN + THEN
        assertViolationOn(request, "email");
    }

    // =====================================
    // 3. FORMATO DE EMAIL INCORRECTO
    // =====================================

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@optiflow.com",
            "user@@optiflow.com",
            "user name@optiflow.com"
    })
    void givenInvalidEmail_whenValidate_thenReject(String email) {

        // GIVEN
        LoginRequest request = validRequest();
        request.setEmail(email);

        // WHEN + THEN
        assertViolationOn(request, "email");
    }

    // =====================================
    // 4. EMAIL CON MAYÚSCULAS
    // =====================================

    @Test
    void givenUppercaseEmail_whenValidate_thenAccept() {

        // GIVEN
        LoginRequest request = validRequest();

        request.setEmail("CARLOS@OPTIFLOW.COM");

        // WHEN + THEN
        assertValid(request);
    }

    // =====================================
    // 5. PASSWORD NULA O VACÍA
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankPassword_whenValidate_thenReject(String password) {

        // GIVEN
        LoginRequest request = validRequest();
        request.setPassword(password);

        // WHEN + THEN
        assertViolationOn(request, "password");
    }

    // =====================================
    // 6. PASSWORD CORTA
    // =====================================

    @Test
    void givenShortPassword_whenValidate_thenAccept() {

        // GIVEN
        LoginRequest request = validRequest();
        request.setPassword("123");

        // WHEN + THEN
        assertValid(request);
    }

    // =====================================
    // 7. PASSWORD CON CARACTERES ESPECIALES
    // =====================================

    @Test
    void givenComplexPassword_whenValidate_thenAccept() {

        // GIVEN
        LoginRequest request = validRequest();

        request.setPassword("P@ssw0rd!#€2026");

        // WHEN + THEN
        assertValid(request);
    }

    // =====================================
    // 8. TODOS LOS CAMPOS NULOS
    // =====================================

    @Test
    void givenAllFieldsNull_whenValidate_thenRejectBoth() {

        // GIVEN
        LoginRequest request = new LoginRequest();

        // WHEN
        Set<String> invalidFields = validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        // THEN
        assertEquals(Set.of("email", "password"), invalidFields);
    }

    // =====================================
    // 9. VARIOS ERRORES SIMULTÁNEOS
    // =====================================

    @Test
    void givenInvalidEmailAndBlankPassword_whenValidate_thenReportBoth() {

        // GIVEN
        LoginRequest request = new LoginRequest();

        request.setEmail("invalid-email");
        request.setPassword("   ");

        // WHEN
        Set<String> invalidFields = validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        // THEN
        assertEquals(Set.of("email", "password"), invalidFields);
    }

    // =====================================
    // 10. MENSAJES DE ERROR PERSONALIZADOS
    // =====================================

    @Test
    void givenMissingEmail_whenValidate_thenReturnExpectedMessage() {

        // GIVEN
        LoginRequest request = validRequest();
        request.setEmail(null);

        // WHEN
        Set<ConstraintViolation<LoginRequest>> violations = validate(request);

        // THEN
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("email")
                                && v.getMessage().equals("El email es obligatorio")));
    }

    @Test
    void givenMissingPassword_whenValidate_thenReturnExpectedMessage() {

        // GIVEN
        LoginRequest request = validRequest();
        request.setPassword(null);

        // WHEN
        Set<ConstraintViolation<LoginRequest>> violations = validate(request);

        // THEN
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("password")
                                && v.getMessage().equals("La contraseña es obligatoria")));
    }

    // =====================================
    // 11. FORMATO DE EMAIL: MENSAJE DE ERROR
    // =====================================

    @Test
    void givenInvalidEmail_whenValidate_thenReturnEmailFormatMessage() {

        // GIVEN
        LoginRequest request = validRequest();

        request.setEmail("invalid-email");

        // WHEN
        Set<ConstraintViolation<LoginRequest>> violations = validate(request);

        // THEN
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("email")
                                && v.getMessage().equals("El email no tiene un formato válido")));
    }
}
