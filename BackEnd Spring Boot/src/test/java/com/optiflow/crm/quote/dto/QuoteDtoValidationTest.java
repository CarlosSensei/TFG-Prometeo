package com.optiflow.crm.quote.dto;

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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuoteDtoValidationTest {

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

    private QuoteDto validQuote() {

        QuoteDto dto = new QuoteDto();

        dto.setQuoteNumber("Q-2026-000001");
        dto.setClientId(7L);

        dto.setSubtotal(new BigDecimal("100.00"));
        dto.setDiscount(BigDecimal.ZERO);

        return dto;
    }

    private QuoteItemDto validItem() {

        QuoteItemDto item = new QuoteItemDto();

        item.setInventoryItemId(5L);
        item.setQuantity(2);

        return item;
    }

    private void assertViolationOn(Object dto, String field) {

        Set<? extends ConstraintViolation<?>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals(field)),
                "Se esperaba error en '" + field + "', pero se obtuvo: " + violations);
    }

    // =====================================
    // PRESUPUESTO VÁLIDO
    // =====================================

    @Test
    void givenValidQuote_whenValidate_thenNoViolations() {

        // GIVEN
        QuoteDto dto = validQuote();

        // WHEN
        Set<ConstraintViolation<QuoteDto>> violations = validator.validate(dto);

        // THEN
        assertTrue(violations.isEmpty(), "El presupuesto debería ser válido: " + violations);
    }

    // =====================================
    // CLIENT ID
    // =====================================

    @Test
    void givenNullClientId_whenValidate_thenReject() {

        QuoteDto dto = validQuote();
        dto.setClientId(null);

        assertViolationOn(dto, "clientId");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void givenInvalidClientId_whenValidate_thenReject(Long clientId) {

        QuoteDto dto = validQuote();
        dto.setClientId(clientId);

        assertViolationOn(dto, "clientId");
    }

    // =====================================
    // QUOTE NUMBER
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankQuoteNumber_whenValidate_thenAccept(String quoteNumber) {

        // GIVEN
        QuoteDto dto = validQuote();
        dto.setQuoteNumber(quoteNumber);

        // WHEN
        Set<ConstraintViolation<QuoteDto>> violations = validator.validate(dto);

        // THEN
        assertTrue(violations.isEmpty(),
                "quoteNumber no debe ser obligatorio porque "
                        + "lo genera automáticamente el backend. "
                        + "Violaciones: " + violations);
    }

    // =====================================
    // SUBTOTAL
    // =====================================

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-1.00", "-100.50"})
    void givenNegativeSubtotal_whenValidate_thenReject(String subtotal) {

        QuoteDto dto = validQuote();

        dto.setSubtotal(new BigDecimal(subtotal));

        assertViolationOn(dto, "subtotal");
    }

    @Test
    void givenZeroSubtotal_whenValidate_thenAccept() {

        QuoteDto dto = validQuote();
        dto.setSubtotal(BigDecimal.ZERO);

        Set<ConstraintViolation<QuoteDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // =====================================
    // DISCOUNT
    // =====================================

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-10.00", "-50.50"})
    void givenNegativeDiscount_whenValidate_thenReject(String discount) {

        QuoteDto dto = validQuote();

        dto.setDiscount(new BigDecimal(discount));

        assertViolationOn(dto, "discount");
    }

    @Test
    void givenZeroDiscount_whenValidate_thenAccept() {

        QuoteDto dto = validQuote();
        dto.setDiscount(BigDecimal.ZERO);

        Set<ConstraintViolation<QuoteDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // =====================================
    // ITEMS
    // =====================================

    @Test
    void givenValidItems_whenValidate_thenAccept() {

        QuoteDto dto = validQuote();

        dto.setItems(List.of(validItem()));

        Set<ConstraintViolation<QuoteDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "Los artículos deberían ser válidos: " + violations);
    }

    // =====================================
    // VALIDACIÓN DIRECTA DE QUOTE ITEM
    // =====================================

    @Test
    void givenNullInventoryItemId_whenValidateItem_thenReject() {

        QuoteItemDto item = validItem();

        item.setInventoryItemId(null);

        assertViolationOn(item, "inventoryItemId");
    }

    @Test
    void givenNullQuantity_whenValidateItem_thenReject() {

        QuoteItemDto item = validItem();

        item.setQuantity(null);

        assertViolationOn(item, "quantity");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void givenInvalidQuantity_whenValidateItem_thenReject(int quantity) {

        QuoteItemDto item = validItem();

        item.setQuantity(quantity);

        assertViolationOn(item, "quantity");
    }

    @Test
    void givenValidItem_whenValidate_thenNoViolations() {

        QuoteItemDto item = validItem();

        Set<ConstraintViolation<QuoteItemDto>> violations = validator.validate(item);

        assertTrue(violations.isEmpty(),
                "El artículo debería ser válido: " + violations);
    }
}
