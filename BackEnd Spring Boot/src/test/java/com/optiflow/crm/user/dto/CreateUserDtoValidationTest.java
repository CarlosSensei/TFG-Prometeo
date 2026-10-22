package com.optiflow.crm.user.dto;

import com.optiflow.crm.user.model.UserRole;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserDtoValidationTest {

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

    private CreateUserDto validDto() {

        CreateUserDto dto = new CreateUserDto();

        dto.setFirstName("Carlos");
        dto.setLastName("García");
        dto.setEmail("carlos@optiflow.com");
        dto.setPassword("Password123!");
        dto.setRole(UserRole.SELLER);

        return dto;
    }

    private Set<ConstraintViolation<CreateUserDto>> validate(CreateUserDto dto) {

        return validator.validate(dto);
    }

    private void assertValid(CreateUserDto dto) {

        // WHEN
        Set<ConstraintViolation<CreateUserDto>> violations = validate(dto);

        // THEN
        assertTrue(violations.isEmpty(), "El DTO debería ser válido: " + violations);
    }

    private void assertViolationOn(CreateUserDto dto, String field) {

        // WHEN
        Set<ConstraintViolation<CreateUserDto>> violations = validate(dto);

        // THEN
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals(field)),
                "Se esperaba una violación en " + field + " pero se obtuvo: " + violations);
    }

    private Set<String> invalidFields(CreateUserDto dto) {

        return validate(dto).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    // =====================================
    // 1. DTO COMPLETO Y VÁLIDO
    // =====================================

    @Test
    void givenValidDto_whenValidate_thenNoViolations() {

        // GIVEN
        CreateUserDto dto = validDto();

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 2. FIRST NAME OBLIGATORIO
    // =====================================

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void givenInvalidFirstName_whenValidate_thenReject(String firstName) {

        // GIVEN
        CreateUserDto dto = validDto();
        dto.setFirstName(firstName);

        // WHEN + THEN
        assertViolationOn(dto, "firstName");
    }

    // =====================================
    // 3. LAST NAME OBLIGATORIO
    // =====================================

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void givenInvalidLastName_whenValidate_thenReject(String lastName) {

        // GIVEN
        CreateUserDto dto = validDto();
        dto.setLastName(lastName);

        // WHEN + THEN
        assertViolationOn(dto, "lastName");
    }

    // =====================================
    // 4. NOMBRES COMPUESTOS
    // =====================================

    @Test
    void givenCompoundNames_whenValidate_thenAccept() {

        // GIVEN
        CreateUserDto dto = validDto();

        dto.setFirstName("María José");
        dto.setLastName("García López");

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 5. EMAIL CON FORMATO INCORRECTO
    // =====================================

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@example.com",
            "user@@example.com",
            "user name@example.com"
    })
    void givenInvalidEmail_whenValidate_thenReject(String email) {

        // GIVEN
        CreateUserDto dto = validDto();
        dto.setEmail(email);

        // WHEN + THEN
        assertViolationOn(dto, "email");
    }

    // =====================================
    // 6. EMAIL OBLIGATORIO
    // =====================================

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"   "})
    void givenMissingEmail_whenValidate_thenReject(String email) {

        // GIVEN
        CreateUserDto dto = validDto();
        dto.setEmail(email);

        // WHEN + THEN
        assertViolationOn(dto, "email");
    }

    // =====================================
    // 7. EMAIL VÁLIDO CON MAYÚSCULAS
    // =====================================

    @Test
    void givenUppercaseEmail_whenValidate_thenAccept() {

        // GIVEN
        CreateUserDto dto = validDto();

        dto.setEmail("CARLOS@OPTIFLOW.COM");

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 8. PASSWORD OBLIGATORIA
    // =====================================

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"   "})
    void givenBlankPassword_whenValidate_thenReject(String password) {

        // GIVEN
        CreateUserDto dto = validDto();
        dto.setPassword(password);

        // WHEN + THEN
        assertViolationOn(dto, "password");
    }

    // =====================================
    // 9. PASSWORD DEMASIADO CORTA
    // =====================================

    @ParameterizedTest
    @ValueSource(strings = {"a", "Abc123", "Abc1234"})
    void givenPasswordShorterThan8_whenValidate_thenReject(String password) {

        // GIVEN
        CreateUserDto dto = validDto();
        dto.setPassword(password);

        // WHEN + THEN
        assertViolationOn(dto, "password");
    }

    // =====================================
    // 10. PASSWORD JUSTO EN EL LÍMITE
    // =====================================

    @Test
    void givenPasswordWith8Characters_whenValidate_thenAccept() {

        // GIVEN
        CreateUserDto dto = validDto();
        dto.setPassword("Abc12345");

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 11. PASSWORD LARGA
    // =====================================

    @Test
    void givenLongPassword_whenValidate_thenAccept() {

        // GIVEN
        CreateUserDto dto = validDto();
        dto.setPassword("Abc12345".repeat(10));

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 12. ROLE OBLIGATORIO
    // =====================================

    @Test
    void givenNullRole_whenValidate_thenReject() {

        // GIVEN
        CreateUserDto dto = validDto();
        dto.setRole(null);

        // WHEN + THEN
        assertViolationOn(dto, "role");
    }

    // =====================================
    // 13. TODOS LOS CAMPOS NULOS
    // =====================================

    @Test
    void givenAllFieldsNull_whenValidate_thenRejectAll() {

        // GIVEN
        CreateUserDto dto = new CreateUserDto();

        // WHEN
        Set<String> fields = invalidFields(dto);

        // THEN
        assertEquals(
                Set.of(
                        "firstName",
                        "lastName",
                        "email",
                        "password",
                        "role"
                ),
                fields);
    }

    // =====================================
    // 14. VARIOS ERRORES SIMULTÁNEOS
    // =====================================

    @Test
    void givenMultipleInvalidFields_whenValidate_thenReportAll() {

        // GIVEN
        CreateUserDto dto = validDto();

        dto.setFirstName("");
        dto.setEmail("invalid-email");
        dto.setPassword("123");
        dto.setRole(null);

        // WHEN
        Set<String> fields = invalidFields(dto);

        // THEN
        assertEquals(
                Set.of(
                        "firstName",
                        "email",
                        "password",
                        "role"
                ),
                fields
        );

        // El apellido continúa siendo válido.
        assertFalse(fields.contains("lastName"));
    }
}
