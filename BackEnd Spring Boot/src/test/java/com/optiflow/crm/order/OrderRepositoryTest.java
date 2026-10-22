package com.optiflow.crm.order;

import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.order.specification.OrderSpecification;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    private Client client1;
    private Client client2;
    private User seller;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        client1 = createClient("Ana", "García", "12345678Z", "600111222");
        client2 = createClient("Luis", "Martín", "87654321X", "600333444");

        seller = new User();
        seller.setFirstName("Carlos");
        seller.setLastName("Vendedor");
        seller.setEmail("seller@test.com");
        seller.setPassword("test-password");
        seller.setRole(UserRole.SELLER);

        entityManager.persist(seller);
        entityManager.flush();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Client createClient(String name, String surname, String dni, String phone) {

        Client client = new Client();

        client.setName(name);
        client.setSurname(surname);
        client.setDni(dni);
        client.setPhoneNumber(phone);
        client.setBirthDate(LocalDate.of(1985, 5, 20));

        entityManager.persist(client);

        return client;
    }

    private Order createOrder(String number, Client client, OrderStatus status,
                              boolean active, LocalDate date) {

        Order order = new Order();

        order.setOrderNumber(number);
        order.setClient(client);
        order.setSeller(seller);
        order.setStatus(status);
        order.setActive(active);
        order.setOrderDate(date);
        order.setExpectedDeliveryDate(date.plusDays(10));

        entityManager.persist(order);

        return order;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    // =====================================
    // 1. SAVE AND FIND BY ID
    // =====================================

    @Test
    void givenOrder_whenSave_thenPersistAndFindById() {

        // GIVEN
        Order order = createOrder("O-2026-000001", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 1));

        Long id = order.getId();
        Long clientId = client1.getId();
        Long sellerId = seller.getId();

        flushAndClear();

        // WHEN
        Optional<Order> result = orderRepository.findById(id);

        // THEN
        assertTrue(result.isPresent());

        Order found = result.get();

        assertAll(
                () -> assertEquals("O-2026-000001", found.getOrderNumber()),
                () -> assertEquals(OrderStatus.PENDING, found.getStatus()),
                () -> assertEquals(clientId, found.getClient().getId()),
                () -> assertEquals(sellerId, found.getSeller().getId()),
                () -> assertNotNull(found.getCreatedAt()),
                () -> assertNotNull(found.getUpdatedAt())
        );
    }

    // =====================================
    // 2. FIND ACTIVE
    // =====================================

    @Test
    void givenActiveAndInactiveOrders_whenFindActive_thenExcludeInactive() {

        // GIVEN
        createOrder("O-2026-000002", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 2));
        createOrder("O-2026-000003", client1, OrderStatus.PENDING,
                false, LocalDate.of(2026, 10, 3));

        flushAndClear();

        // WHEN
        Page<Order> result = orderRepository.findByActiveTrue(PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().stream()
                        .allMatch(o -> Boolean.TRUE.equals(o.getActive())));
        assertEquals("O-2026-000002", result.getContent().getFirst().getOrderNumber());
    }

    // =====================================
    // 3. PAGINATION
    // =====================================

    @Test
    void givenThreeOrders_whenFindActive_thenPaginate() {

        // GIVEN
        createOrder("O-2026-000004", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 4));
        createOrder("O-2026-000005", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 5));
        createOrder("O-2026-000006", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 6));

        flushAndClear();

        // WHEN
        Page<Order> result = orderRepository.findByActiveTrue(PageRequest.of(0, 2));

        // THEN
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    // =====================================
    // 4. FIND BY CLIENT
    // =====================================

    @Test
    void givenDifferentClients_whenFindByClient_thenOnlyActiveOrders() {

        // GIVEN
        createOrder("O-2026-000007", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 7));
        createOrder("O-2026-000008", client1, OrderStatus.PENDING,
                false, LocalDate.of(2026, 10, 8));
        createOrder("O-2026-000009", client2, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 9));

        Long clientId = client1.getId();

        flushAndClear();

        // WHEN
        Page<Order> result = orderRepository.findByClientIdAndActiveTrue(
                clientId, PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("O-2026-000007", result.getContent().getFirst().getOrderNumber());
    }

    // =====================================
    // 5. COUNT BY CLIENT
    // =====================================

    @Test
    void givenOrders_whenCountByClient_thenCountOnlyActive() {

        // GIVEN
        createOrder("O-2026-000010", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 10));
        createOrder("O-2026-000011", client1, OrderStatus.CONFIRMED,
                true, LocalDate.of(2026, 10, 11));
        createOrder("O-2026-000012", client1, OrderStatus.PENDING,
                false, LocalDate.of(2026, 10, 12));
        createOrder("O-2026-000013", client2, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 13));

        Long clientId = client1.getId();

        flushAndClear();

        // WHEN
        Long count = orderRepository.countByClientIdAndActiveTrue(clientId);

        // THEN
        assertEquals(2L, count);
    }

    // =====================================
    // 6. LAST ORDER NUMBER
    // =====================================

    @Test
    void givenOrderNumbers_whenFindLatest_thenReturnHighestNumber() {

        // GIVEN
        createOrder("O-2026-000001", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 1));
        createOrder("O-2026-000015", client1, OrderStatus.CONFIRMED,
                true, LocalDate.of(2026, 10, 15));
        createOrder("O-2026-000009", client2, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 9));
        createOrder("O-2025-000099", client2, OrderStatus.PENDING,
                true, LocalDate.of(2025, 12, 1));

        flushAndClear();

        // WHEN
        Optional<Order> result = orderRepository
                .findTopByOrderNumberStartingWithOrderByOrderNumberDesc("O-2026-");

        // THEN
        assertTrue(result.isPresent());
        assertEquals("O-2026-000015", result.get().getOrderNumber());
    }

    // =====================================
    // 7. EXISTS BY QUOTE ID
    // =====================================

    @Test
    void givenOrderFromQuote_whenExistsByQuoteId_thenReturnTrue() {

        // GIVEN
        Quote quote = new Quote();

        quote.setQuoteNumber("Q-2026-000100");
        quote.setClient(client1);
        quote.setSeller(seller);
        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setQuoteDate(LocalDate.of(2026, 10, 10));
        quote.setExpirationDate(LocalDate.of(2026, 11, 9));

        entityManager.persist(quote);

        Long quoteId = quote.getId();

        assertFalse(orderRepository.existsByQuoteId(quoteId));

        Order order = createOrder("O-2026-000100", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 11));

        order.setQuote(quote);

        flushAndClear();

        // WHEN
        boolean exists = orderRepository.existsByQuoteId(quoteId);

        // THEN
        assertTrue(exists);
    }

    // =====================================
    // 8. SEARCH BY STATUS
    // =====================================

    @Test
    void givenStatusFilter_whenSearch_thenReturnMatchingOrders() {

        // GIVEN
        createOrder("O-2026-000016", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 16));
        createOrder("O-2026-000017", client1, OrderStatus.CONFIRMED,
                true, LocalDate.of(2026, 10, 17));
        createOrder("O-2026-000018", client2, OrderStatus.PENDING,
                false, LocalDate.of(2026, 10, 18));

        flushAndClear();

        OrderSearchDto search = new OrderSearchDto();

        search.setStatus(OrderStatus.PENDING);
        search.setActive(true);

        // WHEN
        Page<Order> result = orderRepository.findAll(OrderSpecification.filterBy(search),
                PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("O-2026-000016", result.getContent().getFirst().getOrderNumber());
    }

    // =====================================
    // 9. SEARCH BY CLIENT AND DATE RANGE
    // =====================================

    @Test
    void givenClientAndDateRange_whenSearch_thenFilterCorrectly() {

        // GIVEN
        createOrder("O-2026-000019", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 9, 15));
        createOrder("O-2026-000020", client1, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 15));
        createOrder("O-2026-000021", client2, OrderStatus.PENDING,
                true, LocalDate.of(2026, 10, 15));

        Long clientId = client1.getId();

        flushAndClear();

        OrderSearchDto search = new OrderSearchDto();

        search.setClientId(clientId);
        search.setOrderDateFrom(LocalDate.of(2026, 10, 1));
        search.setOrderDateTo(LocalDate.of(2026, 10, 31));
        search.setActive(true);

        // WHEN
        Page<Order> result = orderRepository.findAll(OrderSpecification.filterBy(search),
                PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("O-2026-000020", result.getContent().getFirst().getOrderNumber());
    }
}
