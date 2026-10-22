package com.optiflow.crm.order.mapper;

import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.order.dto.OrderItemDto;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemMapperTest {

    private OrderItemMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderItemMapper();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private OrderItem completeItem() {

        InventoryItem inventory = new InventoryItem();
        inventory.setId(5L);

        OrderItem item = new OrderItem();

        item.setId(10L);
        item.setInventoryItem(inventory);
        item.setDescription("Montura graduada");
        item.setQuantity(2);

        item.setUnitPrice(new BigDecimal("80.00"));
        item.setDiscount(new BigDecimal("10.00"));
        item.setTaxRate(new BigDecimal("21.00"));
        item.setSubtotal(new BigDecimal("150.00"));
        item.setTaxAmount(new BigDecimal("31.50"));
        item.setTotal(new BigDecimal("181.50"));

        return item;
    }

    // =====================================
    // 1. CONVERSIÓN COMPLETA
    // =====================================

    @Test
    void givenCompleteItem_whenToDto_thenMapAllFields() {

        // GIVEN
        OrderItem item = completeItem();

        // WHEN
        OrderItemDto dto = mapper.toDto(item);

        // THEN
        assertAll("OrderItem -> OrderItemDto",
                () -> assertEquals(10L, dto.getId()),
                () -> assertEquals(5L, dto.getInventoryItemId()),
                () -> assertEquals("Montura graduada", dto.getDescription()),
                () -> assertEquals(2, dto.getQuantity()),
                () -> assertEquals(new BigDecimal("80.00"), dto.getUnitPrice()),
                () -> assertEquals(new BigDecimal("10.00"), dto.getDiscount()),
                () -> assertEquals(new BigDecimal("21.00"), dto.getTaxRate()),
                () -> assertEquals(new BigDecimal("150.00"), dto.getSubtotal()),
                () -> assertEquals(new BigDecimal("31.50"), dto.getTaxAmount()),
                () -> assertEquals(new BigDecimal("181.50"), dto.getTotal())
        );
    }

    // =====================================
    // 2. INVENTARIO NULO
    // =====================================

    @Test
    void givenItemWithoutInventory_whenToDto_thenInventoryIdIsNull() {

        // GIVEN
        OrderItem item = completeItem();
        item.setInventoryItem(null);

        // WHEN
        OrderItemDto dto = mapper.toDto(item);

        // THEN
        assertNull(dto.getInventoryItemId());

        assertEquals(10L, dto.getId());
        assertEquals("Montura graduada", dto.getDescription());
    }

    // =====================================
    // 3. REFERENCIA AL INVENTARIO
    // =====================================

    @Test
    void givenItemWithInventory_whenToDto_thenMapInventoryId() {

        // GIVEN
        OrderItem item = new OrderItem();

        InventoryItem inventory = new InventoryItem();
        inventory.setId(99L);

        item.setInventoryItem(inventory);

        // WHEN
        OrderItemDto dto = mapper.toDto(item);

        // THEN
        assertEquals(99L, dto.getInventoryItemId());
    }

    // =====================================
    // 4. DESCUENTO CERO
    // =====================================

    @Test
    void givenZeroDiscount_whenToDto_thenPreserveZero() {

        // GIVEN
        OrderItem item = completeItem();
        item.setDiscount(BigDecimal.ZERO);

        // WHEN
        OrderItemDto dto = mapper.toDto(item);

        // THEN
        assertNotNull(dto.getDiscount());

        assertEquals(0, BigDecimal.ZERO.compareTo(dto.getDiscount()));
    }

    // =====================================
    // 5. CAMPOS MONETARIOS NULOS
    // =====================================

    @Test
    void givenNullMonetaryValues_whenToDto_thenPreserveNulls() {

        // GIVEN
        OrderItem item = new OrderItem();

        item.setInventoryItem(null);
        item.setDescription(null);
        item.setQuantity(null);
        item.setUnitPrice(null);
        item.setDiscount(null);
        item.setTaxRate(null);
        item.setSubtotal(null);
        item.setTaxAmount(null);
        item.setTotal(null);

        // WHEN
        OrderItemDto dto = mapper.toDto(item);

        // THEN
        assertAll("Campos nulos",
                () -> assertNull(dto.getId()),
                () -> assertNull(dto.getInventoryItemId()),
                () -> assertNull(dto.getDescription()),
                () -> assertNull(dto.getQuantity()),
                () -> assertNull(dto.getUnitPrice()),
                () -> assertNull(dto.getDiscount()),
                () -> assertNull(dto.getTaxRate()),
                () -> assertNull(dto.getSubtotal()),
                () -> assertNull(dto.getTaxAmount()),
                () -> assertNull(dto.getTotal())
        );
    }

    // =====================================
    // 6. RELACIÓN CON EL PEDIDO
    // =====================================

    @Test
    void givenItemWithOrder_whenToDto_thenMapWithoutOrderTraversal() {

        // GIVEN
        OrderItem item = completeItem();

        Order order = new Order();
        order.setId(25L);

        item.setOrder(order);

        // WHEN
        OrderItemDto dto = mapper.toDto(item);

        // THEN
        assertNotNull(dto);

        assertEquals(10L, dto.getId());
        assertEquals(5L, dto.getInventoryItemId());
        assertEquals("Montura graduada", dto.getDescription());
        assertEquals(new BigDecimal("181.50"), dto.getTotal());

        // OrderItemDto no contiene una referencia a Order.
        // Evitamos relaciones circulares en las respuestas JSON.
    }
}
