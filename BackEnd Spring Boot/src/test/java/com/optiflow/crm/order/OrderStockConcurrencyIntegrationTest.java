package com.optiflow.crm.order;

import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderItem;
import com.optiflow.crm.order.model.OrderService;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.shared.exception.InsufficientStockException;
import com.optiflow.crm.shared.exception.InvalidOrderStatusException;
import com.optiflow.crm.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de concurrencia con transacciones reales, sin mocks y sin
 * @Transactional en la clase: los trabajadores deben ver datos confirmados.
 */
@SpringBootTest(properties = {
        "spring.datasource.hikari.maximum-pool-size=5",
        "spring.datasource.url=jdbc:h2:mem:optiflow_concurrency_test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000"
})
@ActiveProfiles("test")
class OrderStockConcurrencyIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private QuoteToOrderTestDataFactory testDataFactory;
    @Autowired private TransactionTemplate transactionTemplate;

    private Fixture fixture;
    /** Los dos pedidos compiten por las mismas cinco unidades. */
    @Test
    void givenInsufficientSharedStock_whenTwoOrdersConfirm_thenOnlyOneSucceeds()
            throws Exception {

        fixture = createFixture(5, 4, 4);
        Long firstId = fixture.orderIds().get(0);
        Long secondId = fixture.orderIds().get(1);

        List<Outcome> results = runAtSameTime(
                () -> orderService.confirm(firstId),
                () -> orderService.confirm(secondId)
        );

        assertExactlyOneSuccessAndOneFailure(results, InsufficientStockException.class);
        assertEquals(1, persistedStock());

        List<OrderStatus> statuses = List.of(persistedStatus(firstId), persistedStatus(secondId));
        assertEquals(1, statuses.stream().filter(s -> s == OrderStatus.CONFIRMED).count());
        assertEquals(1, statuses.stream().filter(s -> s == OrderStatus.PENDING).count());
    }

    /** Dos peticiones simultaneas sobre el mismo pedido nunca descuentan dos veces. */
    @Test
    void givenSamePendingOrder_whenTwoThreadsConfirm_thenStockDecreasesOnce()
            throws Exception {

        fixture = createFixture(10, 3);
        Long orderId = fixture.orderIds().getFirst();

        List<Outcome> results = runAtSameTime(
                () -> orderService.confirm(orderId),
                () -> orderService.confirm(orderId)
        );

        assertExactlyOneSuccessAndOneFailure(results, InvalidOrderStatusException.class);
        assertEquals(OrderStatus.CONFIRMED, persistedStatus(orderId));
        assertEquals(7, persistedStock());
    }

    /** Dos cancelaciones simultaneas no pueden reponer dos veces el stock. */
    @Test
    void givenConfirmedOrder_whenTwoThreadsCancel_thenStockRestoredOnce()
            throws Exception {

        fixture = createFixture(10, 3);
        Long orderId = fixture.orderIds().getFirst();

        orderService.confirm(orderId);
        assertEquals(7, persistedStock());

        List<Outcome> results = runAtSameTime(
                () -> orderService.cancel(orderId),
                () -> orderService.cancel(orderId)
        );

        assertExactlyOneSuccessAndOneFailure(results, InvalidOrderStatusException.class);
        assertEquals(OrderStatus.CANCELLED, persistedStatus(orderId));
        assertEquals(10, persistedStock());
    }

    /**
     * Crea y confirma datos antes del comienzo de los trabajadores.
     * Se reutiliza la fabrica de pruebas ya existente en OptiFlow.
     */
    private Fixture createFixture(int initialStock, int... quantities) {
        Fixture created = transactionTemplate.execute(tx -> {
            Quote quote = testDataFactory.createQuote(QuoteStatus.ACCEPTED);
            InventoryItem item = quote.getItems().getFirst().getInventoryItem();
            item.setStock(initialStock);
            inventoryRepository.saveAndFlush(item);

            List<Long> orderIds = new ArrayList<>();
            for (int quantity : quantities) {
                Order order = new Order();
                order.setOrderNumber("O-T-" + UUID.randomUUID().toString().substring(0, 12));
                order.setClient(quote.getClient());
                order.setSeller(quote.getSeller());
                order.setStatus(OrderStatus.PENDING);
                order.setOrderDate(LocalDate.now());
                order.setActive(true);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setInventoryItem(item);
                orderItem.setDescription(item.getName());
                orderItem.setQuantity(quantity);
                orderItem.setUnitPrice(item.getSalePrice());
                orderItem.setDiscount(BigDecimal.ZERO);
                orderItem.setTaxRate(item.getTaxRate());
                BigDecimal subtotal = item.getSalePrice()
                        .multiply(BigDecimal.valueOf(quantity));
                BigDecimal tax = subtotal.multiply(item.getTaxRate())
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                orderItem.setSubtotal(subtotal);
                orderItem.setTaxAmount(tax);
                orderItem.setTotal(subtotal.add(tax));
                order.getItems().add(orderItem);

                order.setSubtotal(subtotal);
                order.setTaxAmount(tax);
                order.setTotal(subtotal.add(tax));
                orderIds.add(orderRepository.saveAndFlush(order).getId());
            }
            return new Fixture(
                    item.getId(), quote.getId(), quote.getClient().getId(),
                    quote.getSeller().getId(), List.copyOf(orderIds)
            );
        });
        assertNotNull(created);
        return created;
    }

    /**
     * El latch sincroniza el comienzo de las llamadas. No obliga al motor de
     * base de datos a adquirir los bloqueos en un orden concreto.
     */
    private List<Outcome> runAtSameTime(Callable<?> first, Callable<?> second)
            throws Exception {

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Callable<Outcome> firstWorker = () -> runWorker(first, ready, start);
            Callable<Outcome> secondWorker = () -> runWorker(second, ready, start);

            Future<Outcome> firstResult = executor.submit(firstWorker);
            Future<Outcome> secondResult = executor.submit(secondWorker);

            assertTrue(ready.await(10, TimeUnit.SECONDS),
                    "Los dos trabajadores no llegaron a estar preparados");
            start.countDown();

            return List.of(
                    firstResult.get(20, TimeUnit.SECONDS),
                    secondResult.get(20, TimeUnit.SECONDS)
            );
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS),
                    "Hay un trabajador que no ha terminado");
        }
    }

    private Outcome runWorker(Callable<?> task, CountDownLatch ready,
                              CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(10, TimeUnit.SECONDS)) {
            return new Outcome(false, new IllegalStateException("Timeout de inicio"));
        }
        try {
            task.call();
            return new Outcome(true, null);
        } catch (Exception exception) {
            return new Outcome(false, exception);
        }
    }

    private void assertExactlyOneSuccessAndOneFailure(
            List<Outcome> results, Class<? extends Throwable> expectedException) {

        assertEquals(1, results.stream().filter(Outcome::success).count(),
                () -> "Se esperaba una operacion correcta: " + results);
        assertEquals(1, results.stream()
                        .filter(outcome -> !outcome.success()
                                && expectedException.isInstance(outcome.error()))
                        .count(),
                () -> "La otra operacion debia fallar con "
                        + expectedException.getSimpleName() + ": " + results);
    }

    private int persistedStock() {
        return inventoryRepository.findById(fixture.inventoryId())
                .orElseThrow().getStock();
    }

    private OrderStatus persistedStatus(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow().getStatus();
    }


    @AfterEach
    void cleanUp() {

        if (fixture == null) {
            return;
        }

        transactionTemplate.executeWithoutResult(tx -> {

            // 1. Eliminar pedidos y sus líneas asociadas.
            for (Long orderId : fixture.orderIds()) {
                orderRepository.deleteById(orderId);
            }

            orderRepository.flush();

            // 2. Eliminar presupuesto y sus líneas asociadas.
            quoteRepository.deleteById(fixture.quoteId());
            quoteRepository.flush();

            // 3. Eliminar artículo de inventario.
            inventoryRepository.deleteById(fixture.inventoryId());

            // 4. Eliminar cliente.
            clientRepository.deleteById(fixture.clientId());

            // 5. Eliminar vendedor.
            userRepository.deleteById(fixture.sellerId());
        });

        // Limpiar la referencia para el siguiente test.
        fixture = null;
    }

    private record Fixture(
            Long inventoryId,
            Long quoteId,
            Long clientId,
            Long sellerId,
            List<Long> orderIds
    ) {}

    private record Outcome(
            boolean success,
            Throwable error
    ) {}

}