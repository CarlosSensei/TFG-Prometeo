package com.optiflow.crm.order.domain;

import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderItem;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.shared.exception.InvalidOrderStatusException;
import com.optiflow.crm.shared.exception.InvalidQuoteStatusException;
import com.optiflow.crm.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDomainServiceTest {

    private OrderDomainService service;

    @BeforeEach
    void setUp() {
        service = new OrderDomainService();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private OrderItem createItem(int quantity, String unitPrice,
                                 String discount, String taxRate) {

        OrderItem item = new OrderItem();

        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        item.setDiscount(new BigDecimal(discount));
        item.setTaxRate(new BigDecimal(taxRate));

        return item;
    }

    private void assertMoney(String expected, BigDecimal actual) {

        assertNotNull(actual);

        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "Esperado: " + expected + ", obtenido: " + actual);
    }

    private Quote createAcceptedQuote() {

        Quote quote = new Quote();

        Client client = new Client();
        client.setId(7L);

        User seller = new User();
        seller.setId(3L);

        quote.setClient(client);
        quote.setSeller(seller);
        quote.setStatus(QuoteStatus.ACCEPTED);

        return quote;
    }

    // =====================================
    // 1. CALCULATE ITEM
    // =====================================

    @Test
    void givenValidItem_whenCalculate_thenCalculateAmounts() {

        // GIVEN
        OrderItem item = createItem(
                2, "80.00", "10.00", "21.00");

        // WHEN
        service.calculateItem(item);

        // THEN
        // Bruto: 80 * 2 = 160
        // Descuento: 10
        // Subtotal: 150
        // IVA: 31.50
        // Total: 181.50

        assertMoney("150.00", item.getSubtotal());
        assertMoney("31.50", item.getTaxAmount());
        assertMoney("181.50", item.getTotal());
    }

    @Test
    void givenExcessiveDiscount_whenCalculate_thenSubtotalZero() {

        // GIVEN
        OrderItem item = createItem(
                1, "50.00", "80.00", "21.00");

        // WHEN
        service.calculateItem(item);

        // THEN
        assertMoney("0.00", item.getSubtotal());
        assertMoney("0.00", item.getTaxAmount());
        assertMoney("0.00", item.getTotal());
    }

    @Test
    void givenDecimalTax_whenCalculate_thenRoundHalfUp() {

        // GIVEN
        OrderItem item = createItem(
                1, "0.50", "0.00", "21.00");

        // WHEN
        service.calculateItem(item);

        // THEN
        // 0.50 * 21% = 0.105
        // HALF_UP => 0.11

        assertMoney("0.50", item.getSubtotal());
        assertMoney("0.11", item.getTaxAmount());
        assertMoney("0.61", item.getTotal());
    }

    @Test
    void givenNullMonetaryValues_whenCalculate_thenUseZero() {

        // GIVEN
        OrderItem item = new OrderItem();

        item.setQuantity(2);
        item.setUnitPrice(null);
        item.setDiscount(null);
        item.setTaxRate(null);

        // WHEN
        service.calculateItem(item);

        // THEN
        assertMoney("0.00", item.getSubtotal());
        assertMoney("0.00", item.getTaxAmount());
        assertMoney("0.00", item.getTotal());
    }

    // =====================================
    // 2. CALCULATE TOTALS
    // =====================================

    @Test
    void givenMultipleItems_whenCalculateTotals_thenSumAmounts() {

        // GIVEN
        OrderItem item1 = createItem(
                2, "80.00", "10.00", "21.00");

        OrderItem item2 = createItem(
                1, "100.00", "0.00", "21.00");

        service.calculateItem(item1);
        service.calculateItem(item2);

        Order order = new Order();
        order.setItems(List.of(item1, item2));

        // WHEN
        service.calculateTotals(order);

        // THEN
        assertMoney("250.00", order.getSubtotal());
        assertMoney("52.50", order.getTaxAmount());
        assertMoney("302.50", order.getTotal());
    }

    @Test
    void givenEmptyOrder_whenCalculateTotals_thenReturnZero() {

        // GIVEN
        Order order = new Order();
        order.setItems(List.of());

        // WHEN
        service.calculateTotals(order);

        // THEN
        assertMoney("0.00", order.getSubtotal());
        assertMoney("0.00", order.getTaxAmount());
        assertMoney("0.00", order.getTotal());
    }

    @Test
    void givenNullItemAmounts_whenCalculateTotals_thenIgnoreNulls() {

        // GIVEN
        OrderItem item1 = new OrderItem();

        item1.setSubtotal(new BigDecimal("100.00"));
        item1.setTaxAmount(null);
        item1.setTotal(new BigDecimal("121.00"));

        OrderItem item2 = new OrderItem();

        item2.setSubtotal(null);
        item2.setTaxAmount(new BigDecimal("21.00"));
        item2.setTotal(null);

        Order order = new Order();
        order.setItems(List.of(item1, item2));

        // WHEN
        service.calculateTotals(order);

        // THEN
        assertMoney("100.00", order.getSubtotal());
        assertMoney("21.00", order.getTaxAmount());
        assertMoney("121.00", order.getTotal());
    }

    // =====================================
    // 3. INITIALIZE ORDER
    // =====================================

    @Test
    void givenEmptyOrder_whenInitialize_thenSetDefaults() {

        // GIVEN
        Order order = new Order();

        order.setOrderDate(null);
        order.setStatus(null);
        order.setTaxRate(null);
        order.setActive(null);

        LocalDate before = LocalDate.now();

        // WHEN
        service.initializeOrder(order);

        LocalDate after = LocalDate.now();

        // THEN
        assertNotNull(order.getOrderDate());

        assertFalse(order.getOrderDate().isBefore(before));
        assertFalse(order.getOrderDate().isAfter(after));

        assertEquals(OrderStatus.PENDING, order.getStatus());

        assertMoney("21.00", order.getTaxRate());

        assertTrue(order.getActive());
    }

    @Test
    void givenExistingValues_whenInitialize_thenPreserveValues() {

        // GIVEN
        Order order = new Order();

        LocalDate originalDate = LocalDate.of(2026, 10, 1);

        order.setOrderDate(originalDate);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTaxRate(new BigDecimal("10.00"));
        order.setActive(false);

        // WHEN
        service.initializeOrder(order);

        // THEN
        assertEquals(originalDate, order.getOrderDate());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        assertMoney("10.00", order.getTaxRate());

        assertFalse(order.getActive());
    }

    // =====================================
    // 4. VALIDATE QUOTE CONVERSION
    // =====================================

    @Test
    void givenAcceptedQuote_whenValidateConversion_thenAccept() {

        // GIVEN
        Quote quote = createAcceptedQuote();

        // WHEN + THEN
        assertDoesNotThrow(() -> service.validateQuoteConversion(quote));
    }

    @Test
    void givenDraftQuote_whenValidateConversion_thenThrow() {

        // GIVEN
        Quote quote = new Quote();
        quote.setStatus(QuoteStatus.DRAFT);

        // WHEN + THEN
        InvalidQuoteStatusException exception =
                assertThrows(InvalidQuoteStatusException.class,
                        () -> service.validateQuoteConversion(quote));

        assertTrue(exception.getMessage().contains("ACCEPTED"));
    }

    // =====================================
    // 5. CREATE FROM QUOTE
    // =====================================

    @Test
    void givenAcceptedQuote_whenCreateFromQuote_thenCopyRelations() {

        // GIVEN
        Quote quote = createAcceptedQuote();

        // WHEN
        Order order = service.createFromQuote(quote);

        // THEN
        assertNotNull(order);

        assertSame(quote, order.getQuote());
        assertSame(quote.getClient(), order.getClient());
        assertSame(quote.getSeller(), order.getSeller());

        assertEquals(OrderStatus.PENDING, order.getStatus());

        assertNotNull(order.getOrderDate());
        assertTrue(order.getActive());
        assertNull(order.getId());

        assertMoney("0.00", order.getTotal());
    }

    @Test
    void givenQuoteWithItems_whenCreateFromQuote_thenCopyItems() {

        // GIVEN
        Quote quote = createAcceptedQuote();

        InventoryItem inventory = new InventoryItem();
        inventory.setId(5L);

        QuoteItem quoteItem = new QuoteItem();

        quoteItem.setInventoryItem(inventory);
        quoteItem.setDescription("Montura graduada");
        quoteItem.setQuantity(2);
        quoteItem.setUnitPrice(new BigDecimal("80.00"));
        quoteItem.setDiscount(new BigDecimal("10.00"));
        quoteItem.setTaxRate(new BigDecimal("21.00"));

        quoteItem.setSubtotal(new BigDecimal("150.00"));
        quoteItem.setTaxAmount(new BigDecimal("31.50"));
        quoteItem.setTotal(new BigDecimal("181.50"));

        quote.setItems(List.of(quoteItem));

        // WHEN
        Order order = service.createFromQuote(quote);

        // THEN
        assertEquals(1, order.getItems().size());

        OrderItem orderItem = order.getItems().getFirst();

        assertNotSame(quoteItem, orderItem);

        assertSame(order, orderItem.getOrder());
        assertSame(inventory, orderItem.getInventoryItem());

        assertEquals("Montura graduada", orderItem.getDescription());

        assertEquals(2, orderItem.getQuantity());

        assertMoney("80.00", orderItem.getUnitPrice());
        assertMoney("10.00", orderItem.getDiscount());
        assertMoney("21.00", orderItem.getTaxRate());

        assertMoney("150.00", orderItem.getSubtotal());
        assertMoney("31.50", orderItem.getTaxAmount());
        assertMoney("181.50", orderItem.getTotal());

        assertMoney("150.00", order.getSubtotal());
        assertMoney("31.50", order.getTaxAmount());
        assertMoney("181.50", order.getTotal());
    }

    // =====================================
    // 6. VALIDATE CAN CONFIRM
    // =====================================

    @Test
    void givenPendingOrderWithItems_whenValidateConfirm_thenAccept() {

        // GIVEN
        Order order = new Order();

        order.setStatus(OrderStatus.PENDING);
        order.setItems(List.of(new OrderItem()));

        // WHEN + THEN
        assertDoesNotThrow(() -> service.validateCanConfirm(order));
    }

    @Test
    void givenConfirmedOrder_whenValidateConfirm_thenThrow() {

        // GIVEN
        Order order = new Order();

        order.setStatus(OrderStatus.CONFIRMED);
        order.setItems(List.of(new OrderItem()));

        // WHEN + THEN
        InvalidOrderStatusException exception =
                assertThrows(InvalidOrderStatusException.class,
                        () -> service.validateCanConfirm(order));

        assertTrue(exception.getMessage().contains("PENDING"));
    }

    @Test
    void givenPendingOrderWithoutItems_whenValidateConfirm_thenThrow() {

        // GIVEN
        Order order = new Order();

        order.setStatus(OrderStatus.PENDING);
        order.setItems(List.of());

        // WHEN + THEN
        InvalidOrderStatusException exception =
                assertThrows(InvalidOrderStatusException.class,
                        () -> service.validateCanConfirm(order));

        assertTrue(exception.getMessage().contains("sin artículos"));
    }

    // =====================================
    // 7. VALIDATE CAN CANCEL
    // =====================================

    @Test
    void givenPendingOrder_whenValidateCancel_thenAccept() {

        // GIVEN
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        // WHEN + THEN
        assertDoesNotThrow(() -> service.validateCanCancel(order));
    }

    @Test
    void givenCancelledOrder_whenValidateCancel_thenThrow() {

        // GIVEN
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);

        // WHEN + THEN
        InvalidOrderStatusException exception =
                assertThrows(InvalidOrderStatusException.class,
                        () -> service.validateCanCancel(order));

        assertTrue(exception.getMessage().contains("cancelado"));
    }

    @Test
    void givenDeliveredOrder_whenValidateCancel_thenThrow() {

        // GIVEN
        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERED);

        // WHEN + THEN
        InvalidOrderStatusException exception =
                assertThrows(InvalidOrderStatusException.class,
                        () -> service.validateCanCancel(order));

        assertTrue(exception.getMessage().contains("entregado"));
    }
}
