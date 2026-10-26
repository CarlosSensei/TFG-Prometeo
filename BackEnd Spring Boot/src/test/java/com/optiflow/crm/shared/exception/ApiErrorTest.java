package com.optiflow.crm.shared.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {

    private JsonMapper jsonMapper;

    private final LocalDateTime timestamp =
            LocalDateTime.of(2026, 10, 24, 12, 0);

    @BeforeEach
    void setUp() {

        jsonMapper = JsonMapper.builder().findAndAddModules().build();
    }

    // =====================================
    // 1. CREACIÓN COMPLETA
    // =====================================

    @Test
    void givenAllFields_whenCreateApiError_thenPreserveValues() {

        // GIVEN
        Map<String, String> validationErrors = Map.of(
                "email", "El email es obligatorio",
                "password", "La contraseña es obligatoria"
        );

        // WHEN
        ApiError error = new ApiError(
                timestamp,
                400,
                "Bad Request",
                "Error de validación",
                "/auth/login",
                validationErrors
        );

        // THEN
        assertAll(
                () -> assertEquals(timestamp, error.timestamp()),
                () -> assertEquals(400, error.status()),
                () -> assertEquals("Bad Request", error.error()),
                () -> assertEquals("Error de validación", error.message()),
                () -> assertEquals("/auth/login", error.path()),
                () -> assertEquals(validationErrors, error.validationErrors()));
    }

    // =====================================
    // 2. ERRORES SIN VALIDACIONES
    // =====================================

    @Test
    void givenNotFoundError_whenCreate_thenValidationErrorsCanBeNull() {

        // GIVEN / WHEN
        ApiError error = new ApiError(
                timestamp,
                404,
                "Not Found",
                "Cliente no encontrado",
                "/clients/99",
                null
        );

        // THEN
        assertEquals(404, error.status());
        assertEquals("Cliente no encontrado", error.message());
        assertNull(error.validationErrors());
    }

    // =====================================
    // 3. SERIALIZACIÓN JSON
    // =====================================

    @Test
    void givenApiError_whenSerialize_thenContainExpectedFields() throws Exception {

        // GIVEN
        ApiError error = new ApiError(
                timestamp,
                404,
                "Not Found",
                "Cliente no encontrado",
                "/clients/99",
                null
        );

        // WHEN
        String json = jsonMapper.writeValueAsString(error);
        JsonNode root = jsonMapper.readTree(json);

        // THEN
        assertAll(
                () -> assertEquals("2026-10-24T12:00:00", root.get("timestamp").asString()),
                () -> assertEquals(404, root.get("status").asInt()),
                () -> assertEquals("Not Found", root.get("error").asString()),
                () -> assertEquals("Cliente no encontrado", root.get("message").asString()),
                () -> assertEquals("/clients/99", root.get("path").asString()));
    }

    // =====================================
    // 4. SERIALIZACIÓN DE VALIDACIONES
    // =====================================

    @Test
    void givenValidationErrors_whenSerialize_thenIncludeFieldMessages() throws Exception {

        // GIVEN
        ApiError error = new ApiError(timestamp, 400,
                "Bad Request",
                "Error de validación",
                "/auth/login",
                Map.of(
                        "email", "El email es obligatorio",
                        "password", "La contraseña es obligatoria"
                )
        );

        // WHEN
        JsonNode root = jsonMapper.readTree(jsonMapper.writeValueAsString(error));

        // THEN
        JsonNode errors = root.get("validationErrors");

        assertNotNull(errors);
        assertEquals("El email es obligatorio", errors.get("email").asString());
        assertEquals("La contraseña es obligatoria",
                errors.get("password").asString());
    }

    // =====================================
    // 5. SERIALIZACIÓN SIN VALIDACIONES
    // =====================================

    @Test
    void givenNullValidationErrors_whenSerialize_thenRepresentNullOrOmit() throws Exception {

        // GIVEN
        ApiError error = new ApiError(
                timestamp,
                401,
                "Unauthorized",
                "Es necesario iniciar sesión",
                "/auth/me",
                null
        );

        // WHEN
        JsonNode root = jsonMapper.readTree(jsonMapper.writeValueAsString(error));

        // THEN
        JsonNode validationErrors = root.get("validationErrors");

        // Se admite null o ausencia del campo, dependiendo
        // de la configuración de serialización.
        assertTrue(validationErrors == null || validationErrors.isNull());
    }

    // =====================================
    // 6. SERIALIZACIÓN Y DESERIALIZACIÓN
    // =====================================

    @Test
    void givenApiError_whenSerializeAndDeserialize_thenPreserveData() throws Exception {

        // GIVEN
        ApiError original = new ApiError(
                timestamp,
                409,
                "Conflict",
                "El recurso ya existe",
                "/clients",
                Map.of("dni", "DNI duplicado")
        );

        // WHEN
        String json = jsonMapper.writeValueAsString(original);

        ApiError restored = jsonMapper.readValue(json, ApiError.class);

        // THEN
        assertEquals(original, restored);
    }
}
