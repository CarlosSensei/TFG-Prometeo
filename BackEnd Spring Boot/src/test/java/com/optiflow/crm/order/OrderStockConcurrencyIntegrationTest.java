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

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
 * Tests de integración para comprobar la concurrencia
 * en las operaciones de stock de pedidos.
 * Verifican:
 * 1. Dos pedidos compitiendo por stock insuficiente.
 * 2. Confirmación simultánea del mismo pedido.
 * 3. Cancelación simultánea de un pedido confirmado.
 * También verifican la auditoría de movimientos.
 * Se utilizan:
 * - Spring Boot con contexto real.
 * - H2 como base de datos de pruebas.
 * - Transacciones reales.
 * - Bloqueos pesimistas JPA.
 * - SecurityContext por hilo.
 * No se utiliza @Transactional en la clase porque
 * los hilos necesitan acceder a datos confirmados.
 */
@SpringBootTest(properties = {
        "spring.datasource.hikari.maximum-pool-size=5",
        "spring.datasource.url=" +
                "jdbc:h2:mem:optiflow_concurrency_test;" +
                "DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000"
})
@ActiveProfiles("test")
class OrderStockConcurrencyIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuoteToOrderTestDataFactory testDataFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Fixture fixture;


    // =====================================================
    // TEST 1 - DOS PEDIDOS Y STOCK INSUFICIENTE
    // =====================================================

    @Test
    void givenInsufficientSharedStock_whenTwoOrdersConfirm_thenOnlyOneSucceeds()
            throws Exception {

        // GIVEN
        // Tenemos 5 unidades disponibles.
        // Dos pedidos necesitan 4 unidades cada uno.

        fixture = createFixture(5, 4, 4);

        Long firstId = fixture.orderIds().get(0);
        Long secondId = fixture.orderIds().get(1);

        // WHEN
        // Los dos pedidos intentan confirmarse simultáneamente.

        List<Outcome> results = runAtSameTime(
                () -> orderService.confirm(firstId),
                () -> orderService.confirm(secondId)
        );

        // THEN
        // Solo uno puede confirmarse.

        assertExactlyOneSuccessAndOneFailure(
                results,
                InsufficientStockException.class
        );

        // Stock inicial: 5
        // Stock consumido: 4
        // Stock restante: 1

        assertEquals(1, persistedStock());

        List<OrderStatus> statuses = List.of(
                persistedStatus(firstId),
                persistedStatus(secondId)
        );

        assertEquals(
                1,
                statuses.stream()
                        .filter(status ->
                                status == OrderStatus.CONFIRMED)
                        .count()
        );

        assertEquals(
                1,
                statuses.stream()
                        .filter(status ->
                                status == OrderStatus.PENDING)
                        .count()
        );

        // Solo debe existir una salida auditada.

        assertEquals(
                1,
                movementCount("ORDER_CONFIRM")
        );
    }


    // =====================================================
    // TEST 2 - CONFIRMACIÓN CONCURRENTE DEL MISMO PEDIDO
    // =====================================================

    @Test
    void givenSamePendingOrder_whenTwoThreadsConfirm_thenStockDecreasesOnce()
            throws Exception {

        // GIVEN
        // Stock inicial: 10
        // Pedido: 3 unidades

        fixture = createFixture(10, 3);

        Long orderId = fixture.orderIds().getFirst();

        // WHEN

        List<Outcome> results = runAtSameTime(
                () -> orderService.confirm(orderId),
                () -> orderService.confirm(orderId)
        );

        // THEN
        // Una confirmación correcta y otra rechazada.

        assertExactlyOneSuccessAndOneFailure(
                results,
                InvalidOrderStatusException.class
        );

        assertEquals(
                OrderStatus.CONFIRMED,
                persistedStatus(orderId)
        );

        // El stock solo se descuenta una vez.

        assertEquals(7, persistedStock());

        // Solo se registra una salida de stock.

        assertEquals(
                1,
                movementCount("ORDER_CONFIRM")
        );
    }


    // =====================================================
    // TEST 3 - CANCELACIÓN CONCURRENTE
    // =====================================================

    @Test
    void givenConfirmedOrder_whenTwoThreadsCancel_thenStockRestoredOnce()
            throws Exception {

        // GIVEN

        fixture = createFixture(10, 3);

        Long orderId = fixture.orderIds().getFirst();

        // La confirmación inicial también necesita
        // un usuario autenticado para registrar
        // el movimiento de stock.

        runAsFixtureUser(() -> {
            orderService.confirm(orderId);
            return null;
        });

        assertEquals(7, persistedStock());

        assertEquals(
                1,
                movementCount("ORDER_CONFIRM")
        );

        // WHEN

        List<Outcome> results = runAtSameTime(
                () -> orderService.cancel(orderId),
                () -> orderService.cancel(orderId)
        );

        // THEN

        assertExactlyOneSuccessAndOneFailure(
                results,
                InvalidOrderStatusException.class
        );

        assertEquals(
                OrderStatus.CANCELLED,
                persistedStatus(orderId)
        );

        // Las existencias se devuelven una única vez.

        assertEquals(10, persistedStock());

        // Debe existir una sola devolución auditada.

        assertEquals(
                1,
                movementCount("ORDER_CANCEL")
        );

        // Una salida y una devolución.

        assertEquals(
                2,
                totalMovementCount()
        );
    }


    // =====================================================
    // CREACIÓN DE DATOS PARA LOS TESTS
    // =====================================================

    /**
     * Crea los datos necesarios en una transacción real.
     * Los datos quedan confirmados antes de que
     * comiencen los hilos concurrentes.
     */
    private Fixture createFixture(
            int initialStock,
            int... quantities
    ) {

        Fixture created = transactionTemplate.execute(tx -> {

            // Crear presupuesto aceptado con datos asociados.

            Quote quote = testDataFactory.createQuote(
                    QuoteStatus.ACCEPTED
            );

            // Recuperar el artículo del presupuesto.

            InventoryItem item = quote.getItems()
                    .getFirst()
                    .getInventoryItem();

            item.setStock(initialStock);

            inventoryRepository.saveAndFlush(item);

            List<Long> orderIds = new ArrayList<>();

            // Crear pedidos pendientes.

            for (int quantity : quantities) {

                Order order = new Order();

                order.setOrderNumber(
                        "O-T-" +
                                UUID.randomUUID()
                                        .toString()
                                        .substring(0, 12)
                );

                order.setClient(quote.getClient());
                order.setSeller(quote.getSeller());

                order.setStatus(OrderStatus.PENDING);
                order.setOrderDate(LocalDate.now());
                order.setActive(true);

                // Línea de pedido.

                OrderItem orderItem = new OrderItem();

                orderItem.setOrder(order);
                orderItem.setInventoryItem(item);

                orderItem.setDescription(item.getName());
                orderItem.setQuantity(quantity);

                orderItem.setUnitPrice(item.getSalePrice());
                orderItem.setDiscount(BigDecimal.ZERO);
                orderItem.setTaxRate(item.getTaxRate());

                // Cálculo económico.

                BigDecimal subtotal = item.getSalePrice()
                        .multiply(
                                BigDecimal.valueOf(quantity)
                        );

                BigDecimal tax = subtotal
                        .multiply(item.getTaxRate())
                        .divide(
                                new BigDecimal("100"),
                                2,
                                RoundingMode.HALF_UP
                        );

                orderItem.setSubtotal(subtotal);
                orderItem.setTaxAmount(tax);
                orderItem.setTotal(subtotal.add(tax));

                order.getItems().add(orderItem);

                order.setSubtotal(subtotal);
                order.setTaxAmount(tax);
                order.setTotal(subtotal.add(tax));

                Order saved = orderRepository.saveAndFlush(
                        order
                );

                orderIds.add(saved.getId());
            }

            // Guardamos también el email del vendedor.
            // Se utilizará para autenticar los hilos.

            return new Fixture(
                    item.getId(),
                    quote.getId(),
                    quote.getClient().getId(),
                    quote.getSeller().getId(),
                    quote.getSeller().getEmail(),
                    List.copyOf(orderIds)
            );
        });

        assertNotNull(created);

        return created;
    }


    // =====================================================
    // EJECUCIÓN CONCURRENTE
    // =====================================================

    /**
     * Ejecuta dos operaciones simultáneamente.
     * Cada worker utiliza su propia transacción
     * (gestionada por el servicio Spring)
     * y su propio contexto de seguridad.
     */
    private List<Outcome> runAtSameTime(
            Callable<?> first,
            Callable<?> second
    ) throws Exception {

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {

            Callable<Outcome> firstWorker = () ->
                    runWorker(first, ready, start);

            Callable<Outcome> secondWorker = () ->
                    runWorker(second, ready, start);

            Future<Outcome> firstResult =
                    executor.submit(firstWorker);

            Future<Outcome> secondResult =
                    executor.submit(secondWorker);

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Los dos trabajadores no llegaron a estar preparados"
            );

            // Liberamos los dos hilos simultáneamente.

            start.countDown();

            return List.of(
                    firstResult.get(20, TimeUnit.SECONDS),
                    secondResult.get(20, TimeUnit.SECONDS)
            );

        } finally {

            start.countDown();

            executor.shutdownNow();

            assertTrue(
                    executor.awaitTermination(
                            10,
                            TimeUnit.SECONDS
                    ),
                    "Hay un trabajador que no ha terminado"
            );
        }
    }


    // =====================================================
    // WORKER AUTENTICADO
    // =====================================================

    /**
     * Cada hilo establece su propio SecurityContext.
     * El contexto de seguridad no se comparte
     * automáticamente entre hilos.
     */
    private Outcome runWorker(
            Callable<?> task,
            CountDownLatch ready,
            CountDownLatch start
    ) throws Exception {

        try {

            // Preparar usuario autenticado.

            authenticateFixtureUser();

            ready.countDown();

            if (!start.await(10, TimeUnit.SECONDS)) {

                return new Outcome(
                        false,
                        new IllegalStateException(
                                "Timeout de inicio"
                        )
                );
            }

            try {

                task.call();

                return new Outcome(true, null);

            } catch (Exception exception) {

                return new Outcome(false, exception);
            }

        } finally {

            // Fundamental para evitar que un hilo
            // reutilizado conserve una identidad anterior.

            SecurityContextHolder.clearContext();
        }
    }


    // =====================================================
    // AUTENTICACIÓN PARA LOS TESTS
    // =====================================================

    /**
     * Crea un Authentication válido usando
     * el email del vendedor del fixture.
     * CurrentUserService obtendrá ese email
     * mediante authentication.getName().
     */
    private void authenticateFixtureUser() {

        assertNotNull(
                fixture,
                "El fixture debe existir antes de autenticar"
        );

        assertNotNull(
                fixture.sellerEmail(),
                "El vendedor necesita un email"
        );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        fixture.sellerEmail(),
                        null,
                        List.of()
                );

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }


    /**
     * Ejecuta una operación autenticada
     * en el hilo principal del test.
     * Se utiliza para confirmar inicialmente
     * un pedido antes de probar su cancelación.
     */
    private <T> T runAsFixtureUser(
            Callable<T> action
    ) throws Exception {

        SecurityContext previousContext =
                SecurityContextHolder.getContext();

        try {

            authenticateFixtureUser();

            return action.call();

        } finally {

            SecurityContextHolder.setContext(
                    previousContext
            );
        }
    }


    // =====================================================
    // COMPROBACIÓN DE RESULTADOS
    // =====================================================

    private void assertExactlyOneSuccessAndOneFailure(
            List<Outcome> results,
            Class<? extends Throwable> expectedException
    ) {

        assertEquals(
                1,
                results.stream()
                        .filter(Outcome::success)
                        .count(),
                () -> "Se esperaba una operación correcta: "
                        + results
        );

        assertEquals(
                1,
                results.stream()
                        .filter(outcome ->
                                !outcome.success()
                                        && expectedException.isInstance(
                                        outcome.error()
                                )
                        )
                        .count(),
                () -> "La otra operación debía fallar con "
                        + expectedException.getSimpleName()
                        + ": " + results
        );
    }


    // =====================================================
    // CONSULTAS DE ESTADO PERSISTIDO
    // =====================================================

    private int persistedStock() {

        return inventoryRepository
                .findById(fixture.inventoryId())
                .orElseThrow()
                .getStock();
    }

    private OrderStatus persistedStatus(Long orderId) {

        return orderRepository
                .findById(orderId)
                .orElseThrow()
                .getStatus();
    }


    // =====================================================
    // AUDITORÍA DE MOVIMIENTOS
    // =====================================================

    /**
     * Cuenta los movimientos asociados al artículo
     * y al tipo de evento de pedido.
     * Utiliza JdbcTemplate para comprobar
     * directamente los registros persistidos.
     */
    private long movementCount(String sourceType) {

        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM stock_movements
                WHERE inventory_item_id = ?
                  AND source_type = ?
                """,
                Long.class,
                fixture.inventoryId(),
                sourceType
        );

        return count != null ? count : 0L;
    }


    private long totalMovementCount() {

        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM stock_movements
                WHERE inventory_item_id = ?
                """,
                Long.class,
                fixture.inventoryId()
        );

        return count != null ? count : 0L;
    }


    // =====================================================
    // LIMPIEZA DE DATOS
    // =====================================================

    @AfterEach
    void cleanUp() {

        try {

            if (fixture == null) {
                return;
            }

            transactionTemplate.executeWithoutResult(tx -> {

                // 1. Eliminar movimientos de stock.
                //
                // Deben desaparecer antes que
                // el artículo y el vendedor porque
                // stock_movements contiene referencias
                // a ambas entidades.

                jdbcTemplate.update(
                        """
                        DELETE FROM stock_movements
                        WHERE inventory_item_id = ?
                        """,
                        fixture.inventoryId()
                );

                // 2. Eliminar pedidos.
                //
                // Las líneas asociadas se eliminan
                // mediante las relaciones JPA
                // existentes en el proyecto.

                for (Long orderId : fixture.orderIds()) {

                    orderRepository.deleteById(orderId);
                }

                orderRepository.flush();

                // 3. Eliminar presupuesto y sus líneas.

                quoteRepository.deleteById(
                        fixture.quoteId()
                );

                quoteRepository.flush();

                // 4. Eliminar artículo.

                inventoryRepository.deleteById(
                        fixture.inventoryId()
                );

                inventoryRepository.flush();

                // 5. Eliminar cliente.

                clientRepository.deleteById(
                        fixture.clientId()
                );

                clientRepository.flush();

                // 6. Eliminar vendedor.

                userRepository.deleteById(
                        fixture.sellerId()
                );

                userRepository.flush();
            });

        } finally {

            fixture = null;

            SecurityContextHolder.clearContext();
        }
    }


    // =====================================================
    // RECORDS AUXILIARES
    // =====================================================

    private record Fixture(
            Long inventoryId,
            Long quoteId,
            Long clientId,
            Long sellerId,
            String sellerEmail,
            List<Long> orderIds
    ) {
    }

    private record Outcome(
            boolean success,
            Throwable error
    ) {
    }
}
