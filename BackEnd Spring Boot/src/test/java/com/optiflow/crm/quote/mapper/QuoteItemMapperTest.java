package com.optiflow.crm.quote.mapper;

import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.quote.dto.QuoteItemDto;
import com.optiflow.crm.quote.model.QuoteItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class QuoteItemMapperTest {

    private QuoteItemMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new QuoteItemMapper();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private QuoteItem completeItem() {

        InventoryItem inventory = new InventoryItem();
        inventory.setId(5L);

        QuoteItem item = new QuoteItem();

        item.setId(10L);
        item.setInventoryItem(inventory);
        item.setDescription("Montura graduada");
        item.setQuantity(2);

        item.setUnitPrice(new BigDecimal("80.00"));
        item.setDiscount(new BigDecimal("10.00"));
        item.setTaxRate(new BigDecimal("21.00"));

        item.setSubtotal(new BigDecimal("144.00"));
        item.setTaxAmount(new BigDecimal("30.24"));
        item.setTotal(new BigDecimal("174.24"));

        return item;
    }

    // =====================================
    // CONVERSIÓN COMPLETA
    // =====================================

    @Test
    void givenCompleteItem_whenToDto_thenMapAllFields() {

        // GIVEN
        QuoteItem item = completeItem();

        // WHEN
        QuoteItemDto dto = mapper.toDto(item);

        // THEN
        assertAll("Conversión QuoteItem -> QuoteItemDto",

                () -> assertEquals(10L, dto.getId()),
                () -> assertEquals(5L, dto.getInventoryItemId()),
                () -> assertEquals("Montura graduada", dto.getDescription()),
                () -> assertEquals(2, dto.getQuantity()),
                () -> assertEquals(new BigDecimal("80.00"), dto.getUnitPrice()),
                () -> assertEquals(new BigDecimal("10.00"), dto.getDiscount()),
                () -> assertEquals(new BigDecimal("21.00"), dto.getTaxRate()),
                () -> assertEquals(new BigDecimal("144.00"), dto.getSubtotal()),
                () -> assertEquals(new BigDecimal("30.24"), dto.getTaxAmount()),
                () -> assertEquals(new BigDecimal("174.24"), dto.getTotal()));
    }

    // =====================================
    // INVENTARIO NULO
    // =====================================

    @Test
    void givenItemWithoutInventory_whenToDto_thenInventoryIdIsNull() {

        // GIVEN
        QuoteItem item = completeItem();
        item.setInventoryItem(null);

        // WHEN
        QuoteItemDto dto = mapper.toDto(item);

        // THEN
        assertNull(dto.getInventoryItemId());
        assertEquals(10L, dto.getId());
        assertEquals("Montura graduada", dto.getDescription());
    }

    // =====================================
    // REFERENCIA AL INVENTARIO
    // =====================================

    @Test
    void givenItemWithInventory_whenToDto_thenMapInventoryId() {

        // GIVEN
        QuoteItem item = new QuoteItem();

        InventoryItem inventory = new InventoryItem();
        inventory.setId(99L);

        item.setInventoryItem(inventory);

        // WHEN
        QuoteItemDto dto = mapper.toDto(item);

        // THEN
        assertEquals(99L, dto.getInventoryItemId());
    }

    // =====================================
    // DESCUENTO CERO
    // =====================================

    @Test
    void givenZeroDiscount_whenToDto_thenPreserveZero() {

        // GIVEN
        QuoteItem item = completeItem();
        item.setDiscount(BigDecimal.ZERO);

        // WHEN
        QuoteItemDto dto = mapper.toDto(item);

        // THEN
        assertNotNull(dto.getDiscount());

        assertEquals(0, BigDecimal.ZERO.compareTo(dto.getDiscount()));
    }

    // =====================================
    // CAMPOS NULOS
    // =====================================

    @Test
    void givenNullOptionalValues_whenToDto_thenPreserveNulls() {

        // GIVEN
        QuoteItem item = new QuoteItem();

        item.setDescription(null);
        item.setUnitPrice(null);
        item.setTaxRate(null);
        item.setSubtotal(null);
        item.setTaxAmount(null);
        item.setTotal(null);

        // WHEN
        QuoteItemDto dto = mapper.toDto(item);

        // THEN
        assertAll(
                () -> assertNull(dto.getId()),
                () -> assertNull(dto.getInventoryItemId()),
                () -> assertNull(dto.getDescription()),
                () -> assertNull(dto.getUnitPrice()),
                () -> assertNull(dto.getTaxRate()),
                () -> assertNull(dto.getSubtotal()),
                () -> assertNull(dto.getTaxAmount()),
                () -> assertNull(dto.getTotal())
        );

        // El descuento tiene valor por defecto en la entidad.
        assertEquals(BigDecimal.ZERO, dto.getDiscount());
    }
}
