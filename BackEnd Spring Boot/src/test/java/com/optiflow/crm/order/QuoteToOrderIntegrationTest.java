package com.optiflow.crm.order;

import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;

import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;

import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderItem;
import com.optiflow.crm.order.model.OrderService;
import com.optiflow.crm.order.model.OrderStatus;

import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.QuoteStatus;

import com.optiflow.crm.shared.exception.InvalidQuoteStatusException;
import com.optiflow.crm.shared.exception.QuoteAlreadyConvertedException;
import com.optiflow.crm.shared.exception.QuoteNotFoundException;

import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuoteToOrderIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private EntityManager entityManager;

    private Client client;
    private User seller;
    private InventoryItem inventoryItem;

    // =====================================
    // DATOS DE PRUEBA
    // =====================================

    @BeforeEach
    void setUp() {

        client = new Client();
        client.setName("Ana");
        client.setSurname("García");
        client.setDni("12345678Z");
        client.setPhoneNumber("600123456");
        client.setActive(true);

        client = clientRepository.saveAndFlush(client);

        seller = new User();
        seller.setFirstName("Carlos");
        seller.setLastName("Vendedor");
        seller.setEmail("vendedor@optiflow.com");
        seller.setPassword("test-password");
        seller.setRole(UserRole.SELLER);
        seller.setActive(true);

        seller = userRepository.saveAndFlush(seller);

        inventoryItem = new InventoryItem();
        inventoryItem.setSku("TEST-FRAME-001");
        inventoryItem.setName("Montura de prueba");
        inventoryItem.setType(InventoryItemType.FRAME);
        inventoryItem.setSalePrice(new BigDecimal("100.00"));
        inventoryItem.setTaxRate(new BigDecimal("21.00"));
        inventoryItem.setStock(10);
        inventoryItem.setActive(true);

        inventoryItem =
                inventoryRepository.saveAndFlush(inventoryItem);
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Quote createQuote(QuoteStatus status) {

        Quote quote = new Quote();

        quote.setQuoteNumber("Q-" + java.util.UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase());
        quote.setClient(client);
        quote.setSeller(seller);
        quote.setStatus(status);
        quote.setQuoteDate(LocalDate.now());
        quote.setActive(true);

        QuoteItem item = new QuoteItem();

        item.setQuote(quote);
        item.setInventoryItem(inventoryItem);
        item.setDescription("Montura de prueba");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setDiscount(new BigDecimal("10.00"));
        item.setTaxRate(new BigDecimal("21.00"));

        // 2 x 100 - 10 = 190
        item.setSubtotal(new BigDecimal("190.00"));

        // 190 x 21% = 39,90
        item.setTaxAmount(new BigDecimal("39.90"));

        // 190 + 39,90 = 229,90
        item.setTotal(new BigDecimal("229.90"));

        quote.getItems().add(item);

        quote.setSubtotal(new BigDecimal("190.00"));
        quote.setTaxAmount(new BigDecimal("39.90"));
        quote.setTotal(new BigDecimal("229.90"));

        return quoteRepository.saveAndFlush(quote);
    }

    private void flushAndClear() {

        entityManager.flush();
        entityManager.clear();
    }

    private void assertMoney(String expected, BigDecimal actual) {

        assertNotNull(actual);

        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    // =====================================
    // 1. CONVERSIÓN COMPLETA
    // =====================================

    @Test
    void givenAcceptedQuote_whenConvert_thenPersistOrderAndUpdateQuote() {

        // GIVEN
        Quote quote = createQuote(QuoteStatus.ACCEPTED);
        Long quoteId = quote.getId();

        flushAndClear();

        // WHEN
        Order created = orderService.createFromQuote(quoteId);

        assertNotNull(created.getId());

        flushAndClear();

        // THEN
        Order savedOrder = orderRepository.findById(created.getId()).orElseThrow();
        Quote savedQuote = quoteRepository.findById(quoteId).orElseThrow();

        assertAll(
                () -> assertEquals(OrderStatus.PENDING, savedOrder.getStatus()),
                () -> assertEquals(QuoteStatus.CONVERTED, savedQuote.getStatus()),
                () -> assertEquals(quoteId, savedOrder.getQuote().getId()),
                () -> assertEquals(client.getId(), savedOrder.getClient().getId()),
                () -> assertEquals(seller.getId(), savedOrder.getSeller().getId()),
                () -> assertTrue(savedOrder.getActive()),
                () -> assertNotNull(savedOrder.getOrderNumber())
        );

        assertTrue(savedOrder.getOrderNumber().matches("O-\\d{4}-\\d{6}"));
        assertTrue(orderRepository.existsByQuoteId(quoteId));
    }

    // =====================================
    // 2. COPIA DE ARTÍCULOS Y TOTALES
    // =====================================

    @Test
    void givenAcceptedQuoteWithItems_whenConvert_thenPreserveAmounts() {

        // GIVEN
        Quote quote = createQuote(QuoteStatus.ACCEPTED);

        flushAndClear();

        // WHEN
        Order created = orderService.createFromQuote(quote.getId());

        flushAndClear();

        // THEN
        Order saved = orderRepository.findById(created.getId()).orElseThrow();

        List<OrderItem> items = saved.getItems();

        assertEquals(1, items.size());

        OrderItem item = items.getFirst();

        assertAll(
                () -> assertEquals("Montura de prueba", item.getDescription()),
                () -> assertEquals(2, item.getQuantity()),
                () -> assertEquals(inventoryItem.getId(), item.getInventoryItem().getId()));

        assertMoney("100.00", item.getUnitPrice());
        assertMoney("10.00", item.getDiscount());
        assertMoney("21.00", item.getTaxRate());
        assertMoney("190.00", item.getSubtotal());
        assertMoney("39.90", item.getTaxAmount());
        assertMoney("229.90", item.getTotal());

        assertMoney("190.00", saved.getSubtotal());
        assertMoney("39.90", saved.getTaxAmount());
        assertMoney("229.90", saved.getTotal());
    }

    // =====================================
    // 3. PRESUPUESTO NO ACEPTADO
    // =====================================


    @ParameterizedTest
    @EnumSource(
            value = QuoteStatus.class,
            names = {
                    "DRAFT",
                    "SENT",
                    "REJECTED",
                    "EXPIRED"
            }
    )
    void givenNonAcceptedQuote_whenConvert_thenRejectWithoutCreatingOrder(QuoteStatus status) {

        // GIVEN
        Quote quote = createQuote(status);
        Long quoteId = quote.getId();

        flushAndClear();

        // WHEN
        assertThrows(InvalidQuoteStatusException.class,
                () -> orderService.createFromQuote(quoteId));

        // THEN
        flushAndClear();

        // No se ha generado ningún pedido.
        assertFalse(orderRepository.existsByQuoteId(quoteId));

        // El presupuesto mantiene su estado original.
        Quote persistedQuote = quoteRepository.findById(quoteId).orElseThrow();

        assertEquals(status, persistedQuote.getStatus());
    }


    // =====================================
    // 4. EVITAR CONVERSIONES DUPLICADAS
    // =====================================

    @Test
    void givenAlreadyConvertedQuote_whenConvertAgain_thenReject() {

        // GIVEN
        Quote quote = createQuote(QuoteStatus.ACCEPTED);
        Long quoteId = quote.getId();

        flushAndClear();

        Order firstOrder = orderService.createFromQuote(quoteId);

        flushAndClear();

        // WHEN
        // El presupuesto ya está CONVERTED.
        // La validación actual rechaza primero su estado.
        assertThrows(InvalidQuoteStatusException.class, () -> orderService.createFromQuote(quoteId));

        // THEN
        assertTrue(orderRepository.existsByQuoteId(quoteId));
        assertEquals(quoteId, orderRepository.findById(firstOrder.getId())
                .orElseThrow().getQuote().getId());
        assertEquals(1L, orderRepository.count());
    }

    // =====================================
    // 5. PRESUPUESTO INEXISTENTE
    // =====================================

    @Test
    void givenUnknownQuote_whenConvert_thenThrowQuoteNotFound() {

        // GIVEN
        Long unknownQuoteId = 999999L;

        // WHEN + THEN
        assertThrows(QuoteNotFoundException.class,
                () -> orderService.createFromQuote(unknownQuoteId));
        assertEquals(0L, orderRepository.count());
    }

    // =====================================
    // 6. PRESUPUESTO RECHAZADO NO MODIFICA IMPORTES
    // =====================================


    @Test
    void givenRejectedQuote_whenConvert_thenPreserveOriginalAmounts() {

        // GIVEN
        Quote quote = createQuote(QuoteStatus.REJECTED);
        Long quoteId = quote.getId();

        flushAndClear();

        // WHEN
        assertThrows(InvalidQuoteStatusException.class,
                () -> orderService.createFromQuote(quoteId));

        flushAndClear();

        // THEN
        Quote persistedQuote = quoteRepository.findById(quoteId).orElseThrow();

        assertEquals(QuoteStatus.REJECTED, persistedQuote.getStatus());

        assertMoney("190.00", persistedQuote.getSubtotal());
        assertMoney("39.90", persistedQuote.getTaxAmount());
        assertMoney("229.90", persistedQuote.getTotal());

        assertFalse(orderRepository.existsByQuoteId(quoteId));
    }

}
