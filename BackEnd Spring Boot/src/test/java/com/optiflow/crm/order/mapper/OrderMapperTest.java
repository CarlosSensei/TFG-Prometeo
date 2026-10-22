package com.optiflow.crm.order.mapper;

import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.order.dto.OrderDto;
import com.optiflow.crm.order.dto.OrderItemDto;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderItem;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    @Mock
    private OrderItemMapper orderItemMapper;

    private OrderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderMapper(orderItemMapper);
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Order completeOrder() {

        Order order = new Order();

        order.setId(1L);
        order.setOrderNumber("O-2026-000001");

        Client client = new Client();
        client.setId(7L);
        order.setClient(client);

        User seller = new User();
        seller.setId(3L);
        order.setSeller(seller);

        Quote quote = new Quote();
        quote.setId(5L);
        order.setQuote(quote);

        order.setStatus(OrderStatus.PENDING);

        order.setOrderDate(LocalDate.of(2026, 10, 10));
        order.setExpectedDeliveryDate(LocalDate.of(2026, 10, 20));

        order.setDeliveryDate(null);

        order.setSubtotal(new BigDecimal("150.00"));
        order.setDiscount(new BigDecimal("10.00"));
        order.setTaxRate(new BigDecimal("21.00"));
        order.setTaxAmount(new BigDecimal("29.40"));
        order.setTotal(new BigDecimal("169.40"));

        order.setNotes("Pedido de prueba");
        order.setActive(true);

        order.setCreatedAt(LocalDateTime.of(2026, 10, 10, 10, 0));
        order.setUpdatedAt(LocalDateTime.of(2026, 10, 11, 12, 0));

        return order;
    }

    private OrderDto completeDto() {

        OrderDto dto = new OrderDto();

        dto.setId(10L);
        dto.setOrderNumber("O-2026-000010");

        dto.setClientId(8L);
        dto.setSellerId(4L);
        dto.setQuoteId(6L);

        dto.setStatus(OrderStatus.CONFIRMED);

        dto.setOrderDate(LocalDate.of(2026, 10, 12));
        dto.setExpectedDeliveryDate(LocalDate.of(2026, 10, 25));
        dto.setDeliveryDate(LocalDate.of(2026, 10, 24));
        dto.setSubtotal(new BigDecimal("200.00"));
        dto.setDiscount(new BigDecimal("20.00"));
        dto.setTaxRate(new BigDecimal("21.00"));
        dto.setTaxAmount(new BigDecimal("37.80"));
        dto.setTotal(new BigDecimal("217.80"));

        dto.setNotes("Pedido actualizado");
        dto.setActive(false);

        dto.setCreatedAt(LocalDateTime.of(2026, 10, 1, 9, 0));
        dto.setUpdatedAt(LocalDateTime.of(2026, 10, 5, 16, 0));

        return dto;
    }

    // =====================================
    // 1. TO DTO - CAMPOS COMPLETOS
    // =====================================

    @Test
    void givenCompleteOrder_whenToDto_thenMapAllFields() {

        // GIVEN
        Order order = completeOrder();

        // WHEN
        OrderDto dto = mapper.toDto(order);

        // THEN
        assertAll("Order -> OrderDto",
                () -> assertEquals(order.getId(), dto.getId()),
                () -> assertEquals(order.getOrderNumber(), dto.getOrderNumber()),
                () -> assertEquals(7L, dto.getClientId()),
                () -> assertEquals(5L, dto.getQuoteId()),
                () -> assertEquals(3L, dto.getSellerId()),
                () -> assertEquals(order.getStatus(), dto.getStatus()),
                () -> assertEquals(order.getOrderDate(), dto.getOrderDate()),
                () -> assertEquals(order.getExpectedDeliveryDate(), dto.getExpectedDeliveryDate()),
                () -> assertEquals(order.getDeliveryDate(), dto.getDeliveryDate()),
                () -> assertEquals(order.getSubtotal(), dto.getSubtotal()),
                () -> assertEquals(order.getDiscount(), dto.getDiscount()),
                () -> assertEquals(order.getTaxRate(), dto.getTaxRate()),
                () -> assertEquals(order.getTaxAmount(), dto.getTaxAmount()),
                () -> assertEquals(order.getTotal(), dto.getTotal()),
                () -> assertEquals(order.getNotes(), dto.getNotes()),
                () -> assertEquals(order.getActive(), dto.getActive()),
                () -> assertEquals(order.getCreatedAt(), dto.getCreatedAt()),
                () -> assertEquals(order.getUpdatedAt(), dto.getUpdatedAt())
        );

        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());

        verifyNoInteractions(orderItemMapper);
    }

    // =====================================
    // 2. TO DTO - RELACIONES NULAS
    // =====================================

    @Test
    void givenOrderWithoutRelations_whenToDto_thenIdsAreNull() {

        // GIVEN
        Order order = new Order();

        order.setClient(null);
        order.setQuote(null);

        // WHEN
        OrderDto dto = mapper.toDto(order);

        // THEN
        assertNull(dto.getClientId());
        assertNull(dto.getQuoteId());

        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());

        verifyNoInteractions(orderItemMapper);
    }

    // =====================================
    // 3. TO DTO - ITEMS
    // =====================================

    @Test
    void givenOrderWithItems_whenToDto_thenMapItems() {

        // GIVEN
        Order order = completeOrder();

        OrderItem item1 = new OrderItem();
        OrderItem item2 = new OrderItem();

        OrderItemDto dto1 = new OrderItemDto();
        dto1.setId(101L);

        OrderItemDto dto2 = new OrderItemDto();
        dto2.setId(102L);

        order.setItems(List.of(item1, item2));

        when(orderItemMapper.toDto(item1)).thenReturn(dto1);
        when(orderItemMapper.toDto(item2)).thenReturn(dto2);

        // WHEN
        OrderDto result = mapper.toDto(order);

        // THEN
        assertEquals(2, result.getItems().size());

        assertSame(dto1, result.getItems().get(0));
        assertSame(dto2, result.getItems().get(1));

        verify(orderItemMapper).toDto(item1);
        verify(orderItemMapper).toDto(item2);

        verifyNoMoreInteractions(orderItemMapper);
    }

    // =====================================
    // 4. TO ENTITY - CAMPOS EDITABLES
    // =====================================

    @Test
    void givenCompleteDto_whenToEntity_thenMapEditableFields() {

        // GIVEN
        OrderDto dto = completeDto();

        // WHEN
        Order order = mapper.toEntity(dto);

        // THEN
        assertNotNull(order);

        assertAll("OrderDto -> Order",
                () -> assertEquals(dto.getStatus(), order.getStatus()),
                () -> assertEquals(dto.getOrderDate(), order.getOrderDate()),
                () -> assertEquals(dto.getExpectedDeliveryDate(), order.getExpectedDeliveryDate()),
                () -> assertEquals(dto.getDeliveryDate(), order.getDeliveryDate()),
                () -> assertEquals(dto.getSubtotal(), order.getSubtotal()),
                () -> assertEquals(dto.getDiscount(), order.getDiscount()),
                () -> assertEquals(dto.getTaxRate(), order.getTaxRate()),
                () -> assertEquals(dto.getNotes(), order.getNotes())
        );

        verifyNoInteractions(orderItemMapper);
    }

    // =====================================
    // 5. TO ENTITY - CAMPOS PROTEGIDOS
    // =====================================

    @Test
    void givenDtoWithControlledFields_whenToEntity_thenIgnoreThem() {

        // GIVEN
        OrderDto dto = completeDto();

        // WHEN
        Order order = mapper.toEntity(dto);

        // THEN
        assertNull(order.getId());
        assertNull(order.getOrderNumber());

        assertNull(order.getClient());
        assertNull(order.getSeller());
        assertNull(order.getQuote());

        assertNotNull(order.getItems());
        assertTrue(order.getItems().isEmpty());

        assertEquals(BigDecimal.ZERO, order.getTaxAmount());
        assertEquals(BigDecimal.ZERO, order.getTotal());

        assertTrue(order.getActive());

        assertNull(order.getCreatedAt());
        assertNull(order.getUpdatedAt());

        verifyNoInteractions(orderItemMapper);
    }

    // =====================================
    // 6. UPDATE - CAMPOS EDITABLES
    // =====================================

    @Test
    void givenDto_whenUpdateEntity_thenUpdateEditableFields() {

        // GIVEN
        OrderDto dto = completeDto();

        Order existing = completeOrder();

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertAll("Campos editables",
                () -> assertEquals(dto.getOrderDate(), existing.getOrderDate()),
                () -> assertEquals(dto.getExpectedDeliveryDate(), existing.getExpectedDeliveryDate()),
                () -> assertEquals(dto.getDeliveryDate(), existing.getDeliveryDate()),
                () -> assertEquals(dto.getSubtotal(), existing.getSubtotal()),
                () -> assertEquals(dto.getDiscount(), existing.getDiscount()),
                () -> assertEquals(dto.getTaxRate(), existing.getTaxRate()),
                () -> assertEquals(dto.getNotes(), existing.getNotes())
        );

        verifyNoInteractions(orderItemMapper);
    }

    // =====================================
    // 7. UPDATE - CAMPOS PROTEGIDOS
    // =====================================

    @Test
    void givenDto_whenUpdateEntity_thenPreserveControlledFields() {

        // GIVEN
        OrderDto dto = completeDto();

        Order existing = completeOrder();

        Client originalClient = existing.getClient();
        User originalSeller = existing.getSeller();
        Quote originalQuote = existing.getQuote();

        Long originalId = existing.getId();
        String originalNumber = existing.getOrderNumber();

        OrderStatus originalStatus = existing.getStatus();

        BigDecimal originalTaxAmount = existing.getTaxAmount();
        BigDecimal originalTotal = existing.getTotal();
        LocalDateTime originalCreated = existing.getCreatedAt();
        LocalDateTime originalUpdated = existing.getUpdatedAt();

        OrderItem item = new OrderItem();
        existing.setItems(List.of(item));

        // WHEN
        mapper.updateEntity(dto, existing);

        // THEN
        assertAll("Campos protegidos",
                () -> assertEquals(originalId, existing.getId()),
                () -> assertEquals(originalNumber, existing.getOrderNumber()),
                () -> assertSame(originalClient, existing.getClient()),
                () -> assertSame(originalSeller, existing.getSeller()),
                () -> assertSame(originalQuote, existing.getQuote()),
                () -> assertEquals(originalStatus, existing.getStatus()),
                () -> assertSame(item, existing.getItems().getFirst()),
                () -> assertEquals(originalTaxAmount, existing.getTaxAmount()),
                () -> assertEquals(originalTotal, existing.getTotal()),
                () -> assertTrue(existing.getActive()),
                () -> assertEquals(originalCreated, existing.getCreatedAt()),
                () -> assertEquals(originalUpdated, existing.getUpdatedAt())
        );

        verifyNoInteractions(orderItemMapper);
    }

    // =====================================
    // 8. FROM QUOTE - RELACIONES Y ESTADO
    // =====================================

    @Test
    void givenQuote_whenFromQuote_thenCreatePendingOrder() {

        // GIVEN
        Client client = new Client();
        client.setId(7L);

        Quote quote = new Quote();
        quote.setId(5L);
        quote.setClient(client);

        // WHEN
        Order order = mapper.fromQuote(quote);

        // THEN
        assertNotNull(order);

        assertSame(quote, order.getQuote());
        assertSame(client, order.getClient());

        assertEquals(OrderStatus.PENDING, order.getStatus());

        assertNull(order.getId());
        assertNull(order.getOrderNumber());

        verifyNoInteractions(orderItemMapper);
    }

    // =====================================
    // 9. FROM QUOTE - DATOS ECONÓMICOS
    // =====================================

    @Test
    void givenQuoteWithAmounts_whenFromQuote_thenCopyAmounts() {

        // GIVEN
        Quote quote = new Quote();

        quote.setSubtotal(new BigDecimal("150.00"));
        quote.setDiscount(new BigDecimal("10.00"));
        quote.setTaxRate(new BigDecimal("21.00"));
        quote.setTaxAmount(new BigDecimal("29.40"));
        quote.setTotal(new BigDecimal("169.40"));

        quote.setNotes("Presupuesto aceptado");

        // WHEN
        Order order = mapper.fromQuote(quote);

        // THEN
        assertAll("Importes del presupuesto",
                () -> assertEquals(quote.getSubtotal(), order.getSubtotal()),
                () -> assertEquals(quote.getDiscount(), order.getDiscount()),
                () -> assertEquals(quote.getTaxRate(), order.getTaxRate()),
                () -> assertEquals(quote.getTaxAmount(), order.getTaxAmount()),
                () -> assertEquals(quote.getTotal(), order.getTotal()),
                () -> assertEquals(quote.getNotes(), order.getNotes())
        );

        assertTrue(order.getItems().isEmpty());

        verifyNoInteractions(orderItemMapper);
    }
}
