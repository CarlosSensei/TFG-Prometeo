package com.optiflow.crm.shared.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        handler = new GlobalExceptionHandler();

        request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn("/api/test");
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private ApiError assertError(
            ResponseEntity<ApiError> response,
            HttpStatus expectedStatus,
            String expectedMessage) {

        assertEquals(expectedStatus.value(), response.getStatusCode().value());

        ApiError error = response.getBody();

        assertNotNull(error);

        assertAll(
                () -> assertNotNull(error.timestamp()),
                () -> assertEquals(expectedStatus.value(), error.status()),
                () -> assertEquals(expectedStatus.getReasonPhrase(), error.error()),
                () -> assertEquals(expectedMessage, error.message()),
                () -> assertEquals("/api/test", error.path()));

        return error;
    }

    // =====================================
    // 1. RECURSO NO ENCONTRADO
    // =====================================

    @Test
    void givenResourceNotFound_whenHandle_thenReturn404() {

        // GIVEN
        ResourceNotFoundException exception =
                new ResourceNotFoundException("Cliente no encontrado");

        // WHEN
        ResponseEntity<ApiError> response = handler.handleResourceNotFound(
                exception, request);

        // THEN
        ApiError error = assertError(response, HttpStatus.NOT_FOUND,
                "Cliente no encontrado");

        assertNull(error.validationErrors());
    }

    // =====================================
    // 2. ERRORES DE VALIDACIÓN
    // =====================================

    @Test
    void givenInvalidDto_whenHandle_thenReturn400WithFieldErrors() {

        // GIVEN
        BindingResult bindingResult = new BeanPropertyBindingResult(
                new Object(), "loginRequest");

        bindingResult.addError(new FieldError("loginRequest",
                "email", "El email es obligatorio"));

        bindingResult.addError(new FieldError("loginRequest",
                "password", "La contraseña es obligatoria"));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);

        // WHEN
        ResponseEntity<ApiError> response = handler.handleValidationErrors(exception, request);

        // THEN
        ApiError error = assertError(response, HttpStatus.BAD_REQUEST,
                "Error de validación");

        assertNotNull(error.validationErrors());
        assertEquals(2, error.validationErrors().size());
        assertEquals("El email es obligatorio", error.validationErrors().get("email"));
        assertEquals("La contraseña es obligatoria", error.validationErrors().get("password"));
    }

    // =====================================
    // 3. INTEGRIDAD DE DATOS
    // =====================================

    @Test
    void givenDataIntegrityViolation_whenHandle_thenReturn409() {

        // GIVEN
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("duplicate key value violates unique constraint");

        // WHEN
        ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(exception, request);

        // THEN
        ApiError error = assertError(response, HttpStatus.CONFLICT,
                "La operación viola una restricción de integridad de los datos");

        // No exponer detalles SQL al cliente.
        assertFalse(error.message().contains("duplicate key"));
        assertNull(error.validationErrors());
    }

    // =====================================
    // 4. ARGUMENTO INVÁLIDO
    // =====================================

    @Test
    void givenIllegalArgument_whenHandle_thenReturn400() {

        // GIVEN
        IllegalArgumentException exception =
                new IllegalArgumentException("La cantidad debe ser positiva");

        // WHEN
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(exception, request);

        // THEN
        ApiError error = assertError(response, HttpStatus.BAD_REQUEST,
                "La cantidad debe ser positiva");

        assertNull(error.validationErrors());
    }

    // =====================================
    // 5. CREDENCIALES INCORRECTAS
    // =====================================

    @Test
    void givenBadCredentials_whenHandle_thenReturn401() {

        // GIVEN
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // WHEN
        ResponseEntity<ApiError> response = handler.handleBadCredentials(exception, request);

        // THEN
        ApiError error = assertError(response, HttpStatus.UNAUTHORIZED,
                "Email o contraseña incorrectos");

        assertFalse(error.message().contains("Bad credentials"));
        assertNull(error.validationErrors());
    }

    // =====================================
    // 6. USUARIO DESACTIVADO
    // =====================================

    @Test
    void givenDisabledUser_whenHandle_thenReturn403() {

        // GIVEN
        DisabledException exception = new DisabledException("Account disabled");

        // WHEN
        ResponseEntity<ApiError> response = handler.handleDisabledUser(exception, request);

        // THEN
        ApiError error = assertError(response, HttpStatus.FORBIDDEN,
                "El usuario está desactivado");

        assertNull(error.validationErrors());
    }

    // =====================================
    // 7. ERROR INESPERADO
    // =====================================

    @Test
    void givenUnexpectedException_whenHandle_thenReturn500() {

        // GIVEN
        Exception exception = new RuntimeException("Database password: secret123");

        // WHEN
        ResponseEntity<ApiError> response = handler.handleGenericException(exception, request);

        // THEN
        ApiError error = assertError(response, HttpStatus.INTERNAL_SERVER_ERROR,
                "Se ha producido un error interno en el servidor");

        // Un error inesperado nunca debe exponer
        // detalles internos de la aplicación.
        assertFalse(error.message().contains("secret123"));
        assertNull(error.validationErrors());
    }
}
