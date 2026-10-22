package com.optiflow.crm.order.dto;

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

class OrderDtoValidationTest {

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

    private OrderDto validOrder() {
        OrderDto dto = new OrderDto();
        dto.setClientId(7L);
        dto.setSubtotal(new BigDecimal("100.00"));
        dto.setDiscount(BigDecimal.ZERO);
        return dto;
    }

    private OrderItemDto validItem() {
        OrderItemDto item = new OrderItemDto();
        item.setInventoryItemId(5L);
        item.setQuantity(2);
        return item;
    }

    private void assertViolationOn(Object dto, String field) {

        Set<? extends ConstraintViolation<?>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals(field)),
                "Se esperaba una violación en '" + field
                        + "', pero se obtuvo: " + violations);
    }

    // =====================================
    // PEDIDO VÁLIDO
    // =====================================

    @Test
    void givenValidOrder_whenValidate_thenNoViolations() {

        OrderDto dto = validOrder();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "El pedido debería ser válido: " + violations);
    }

    // =====================================
    // CLIENT ID
    // =====================================

    @Test
    void givenNullClientId_whenValidate_thenReject() {

        OrderDto dto = validOrder();
        dto.setClientId(null);

        assertViolationOn(dto, "clientId");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void givenNonPositiveClientId_whenValidate_thenReject(long clientId) {

        OrderDto dto = validOrder();
        dto.setClientId(clientId);

        assertViolationOn(dto, "clientId");
    }

    // =====================================
    // QUOTE ID - OPCIONAL
    // =====================================

    @Test
    void givenNullQuoteId_whenValidate_thenAccept() {

        OrderDto dto = validOrder();
        dto.setQuoteId(null);

        assertTrue(validator.validate(dto).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -50L})
    void givenInvalidQuoteId_whenValidate_thenReject(long quoteId) {

        OrderDto dto = validOrder();
        dto.setQuoteId(quoteId);

        assertViolationOn(dto, "quoteId");
    }

    @Test
    void givenPositiveQuoteId_whenValidate_thenAccept() {

        OrderDto dto = validOrder();
        dto.setQuoteId(15L);

        assertTrue(validator.validate(dto).isEmpty());
    }

    // =====================================
    // ORDER NUMBER
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankOrderNumber_whenValidate_thenAccept(String orderNumber) {

        OrderDto dto = validOrder();
        dto.setOrderNumber(orderNumber);

        assertTrue(validator.validate(dto).isEmpty(),
                "orderNumber debe poder omitirse porque " + "lo genera el backend");
    }

    // =====================================
    // SUBTOTAL
    // =====================================

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-1.00", "-100.50"})
    void givenNegativeSubtotal_whenValidate_thenReject(String subtotal) {

        OrderDto dto = validOrder();
        dto.setSubtotal(new BigDecimal(subtotal));

        assertViolationOn(dto, "subtotal");
    }

    @Test
    void givenZeroSubtotal_whenValidate_thenAccept() {

        OrderDto dto = validOrder();
        dto.setSubtotal(BigDecimal.ZERO);

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void givenNullSubtotal_whenValidate_thenAccept() {

        OrderDto dto = validOrder();
        dto.setSubtotal(null);

        assertTrue(validator.validate(dto).isEmpty());
    }

    // =====================================
    // DISCOUNT
    // =====================================

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-10.00", "-50.50"})
    void givenNegativeDiscount_whenValidate_thenReject(String discount) {

        OrderDto dto = validOrder();
        dto.setDiscount(new BigDecimal(discount));

        assertViolationOn(dto, "discount");
    }

    @Test
    void givenZeroDiscount_whenValidate_thenAccept() {

        OrderDto dto = validOrder();
        dto.setDiscount(BigDecimal.ZERO);

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void givenNullDiscount_whenValidate_thenAccept() {

        OrderDto dto = validOrder();
        dto.setDiscount(null);

        assertTrue(validator.validate(dto).isEmpty());
    }

    // =====================================
    // ITEMS
    // =====================================

    @Test
    void givenValidItems_whenValidateOrder_thenAccept() {

        OrderDto dto = validOrder();
        dto.setItems(List.of(validItem()));

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void givenNullItems_whenValidateOrder_thenAccept() {

        OrderDto dto = validOrder();
        dto.setItems(null);

        assertTrue(validator.validate(dto).isEmpty());
    }

    // =====================================
    // VALIDACIÓN DIRECTA DE ORDER ITEM
    // =====================================

    @Test
    void givenValidItem_whenValidate_thenNoViolations() {

        OrderItemDto item = validItem();

        assertTrue(validator.validate(item).isEmpty());
    }

    @Test
    void givenNullInventoryItemId_whenValidateItem_thenReject() {

        OrderItemDto item = validItem();
        item.setInventoryItemId(null);

        assertViolationOn(item, "inventoryItemId");
    }

    @Test
    void givenNullQuantity_whenValidateItem_thenReject() {

        OrderItemDto item = validItem();
        item.setQuantity(null);

        assertViolationOn(item, "quantity");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void givenInvalidQuantity_whenValidateItem_thenReject(int quantity) {

        OrderItemDto item = validItem();
        item.setQuantity(quantity);

        assertViolationOn(item, "quantity");
    }

    @Test
    void givenPositiveQuantity_whenValidateItem_thenAccept() {

        OrderItemDto item = validItem();
        item.setQuantity(5);

        assertTrue(validator.validate(item).isEmpty());
    }
}
