package com.optiflow.crm.inventory.dto;

import com.optiflow.crm.inventory.model.InventoryItemType;

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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDtoValidationTest {

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

    private InventoryItemDto validItem() {

        InventoryItemDto dto = new InventoryItemDto();

        dto.setSku("MON-001");
        dto.setName("Montura graduada");
        dto.setType(InventoryItemType.FRAME);
        dto.setBrand("Ray-Ban");
        dto.setDescription("Montura de acetato");

        dto.setSalePrice(new BigDecimal("120.00"));
        dto.setTaxRate(new BigDecimal("21.00"));
        dto.setStock(10);

        return dto;
    }

    private void assertViolationOn(InventoryItemDto dto, String field) {

        Set<ConstraintViolation<InventoryItemDto>> violations = validator.validate(dto);

        assertTrue(
                violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals(field)),
                "Se esperaba un error en '" + field + "', pero se obtuvo: " + violations);
    }

    private void assertValid(InventoryItemDto dto) {

        Set<ConstraintViolation<InventoryItemDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "El DTO debería ser válido: " + violations);
    }

    // =====================================
    // 1. DTO VÁLIDO
    // =====================================

    @Test
    void givenValidItem_whenValidate_thenNoViolations() {

        // GIVEN
        InventoryItemDto dto = validItem();

        // WHEN + THEN
        assertValid(dto);
    }

    // =====================================
    // 2. SKU OBLIGATORIO
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankSku_whenValidate_thenReject(String sku) {

        InventoryItemDto dto = validItem();
        dto.setSku(sku);

        assertViolationOn(dto, "sku");
    }

    // =====================================
    // 3. LONGITUD DEL SKU
    // =====================================

    @Test
    void givenSkuLongerThan50_whenValidate_thenReject() {

        InventoryItemDto dto = validItem();
        dto.setSku("A".repeat(51));

        assertViolationOn(dto, "sku");
    }

    @Test
    void givenSkuWith50Characters_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();
        dto.setSku("A".repeat(50));

        assertValid(dto);
    }

    // =====================================
    // 4. NOMBRE OBLIGATORIO
    // =====================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenBlankName_whenValidate_thenReject(String name) {

        InventoryItemDto dto = validItem();
        dto.setName(name);

        assertViolationOn(dto, "name");
    }

    // =====================================
    // 5. LONGITUD DEL NOMBRE
    // =====================================

    @Test
    void givenNameLongerThan150_whenValidate_thenReject() {

        InventoryItemDto dto = validItem();
        dto.setName("A".repeat(151));

        assertViolationOn(dto, "name");
    }

    @Test
    void givenNameWith150Characters_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();
        dto.setName("A".repeat(150));

        assertValid(dto);
    }

    // =====================================
    // 6. TIPO OBLIGATORIO
    // =====================================

    @Test
    void givenNullType_whenValidate_thenReject() {

        InventoryItemDto dto = validItem();
        dto.setType(null);

        assertViolationOn(dto, "type");
    }

    // =====================================
    // 7. SERVICIOS VÁLIDOS
    // =====================================

    @Test
    void givenServiceType_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();

        dto.setType(InventoryItemType.SERVICE);
        dto.setStock(0);

        assertValid(dto);
    }

    // =====================================
    // 8. MARCA OPCIONAL
    // =====================================

    @Test
    void givenNullBrand_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();
        dto.setBrand(null);

        assertValid(dto);
    }

    @Test
    void givenBrandLongerThan100_whenValidate_thenReject() {

        InventoryItemDto dto = validItem();
        dto.setBrand("A".repeat(101));

        assertViolationOn(dto, "brand");
    }

    @Test
    void givenBrandWith100Characters_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();
        dto.setBrand("A".repeat(100));

        assertValid(dto);
    }

    // =====================================
    // 9. DESCRIPCIÓN OPCIONAL
    // =====================================

    @Test
    void givenNullDescription_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();
        dto.setDescription(null);

        assertValid(dto);
    }

    @Test
    void givenDescriptionLongerThan500_whenValidate_thenReject() {

        InventoryItemDto dto = validItem();
        dto.setDescription("A".repeat(501));

        assertViolationOn(dto, "description");
    }

    @Test
    void givenDescriptionWith500Characters_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();
        dto.setDescription("A".repeat(500));

        assertValid(dto);
    }

    // =====================================
    // 10. PRECIO DE VENTA
    // =====================================

    @Test
    void givenNullSalePrice_whenValidate_thenReject() {

        InventoryItemDto dto = validItem();
        dto.setSalePrice(null);

        assertViolationOn(dto, "salePrice");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-1.00", "-100.50"})
    void givenNegativeSalePrice_whenValidate_thenReject(String price) {

        InventoryItemDto dto = validItem();
        dto.setSalePrice(new BigDecimal(price));

        assertViolationOn(dto, "salePrice");
    }

    @Test
    void givenZeroSalePrice_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();
        dto.setSalePrice(BigDecimal.ZERO);

        assertValid(dto);
    }

    // =====================================
    // 11. IVA
    // =====================================

    @Test
    void givenNullTaxRate_whenValidate_thenReject() {

        InventoryItemDto dto = validItem();
        dto.setTaxRate(null);

        assertViolationOn(dto, "taxRate");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-1.00", "-21.00"})
    void givenNegativeTaxRate_whenValidate_thenReject(String taxRate) {

        InventoryItemDto dto = validItem();
        dto.setTaxRate(new BigDecimal(taxRate));

        assertViolationOn(dto, "taxRate");
    }

    @Test
    void givenZeroTaxRate_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();
        dto.setTaxRate(BigDecimal.ZERO);

        assertValid(dto);
    }

    // =====================================
    // 12. STOCK
    // =====================================

    @Test
    void givenNullStock_whenValidate_thenReject() {

        InventoryItemDto dto = validItem();
        dto.setStock(null);

        assertViolationOn(dto, "stock");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -5, -100})
    void givenNegativeStock_whenValidate_thenReject(int stock) {

        InventoryItemDto dto = validItem();
        dto.setStock(stock);

        assertViolationOn(dto, "stock");
    }

    @Test
    void givenZeroStock_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();
        dto.setStock(0);

        assertValid(dto);
    }

    // =====================================
    // 13. CAMPOS SIN RESTRICCIONES
    // =====================================

    @Test
    void givenNullOptionalMetadata_whenValidate_thenAccept() {

        InventoryItemDto dto = validItem();

        dto.setId(null);
        dto.setActive(null);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);

        assertValid(dto);
    }
}
