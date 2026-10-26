
package com.optiflow.crm.order;

import com.optiflow.crm.order.model.OrderService;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class QuoteToOrderRollbackIntegrationTest {

    @Autowired
    private OrderService orderService;

    @MockitoSpyBean
    private QuoteRepository quoteRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // Preparar con una fábrica de datos reutilizable.
    @Autowired
    private QuoteToOrderTestDataFactory testDataFactory;

    @Test
    void givenFailureAfterSavingOrder_whenConvert_thenRollbackEverything() {

        // GIVEN
        // Los datos iniciales se confirman antes
        // de comenzar la transacción del servicio.
        Long quoteId = transactionTemplate.execute(status -> {
            Quote quote = testDataFactory.createQuote(QuoteStatus.ACCEPTED);
            return quote.getId();
        });

        assertNotNull(quoteId);

        // Provocamos un error después de guardar el pedido.
        doThrow(new IllegalStateException(
                "Fallo simulado al guardar presupuesto"
        )).when(quoteRepository).save(argThat(quote ->
                quote != null && quote.getStatus() == QuoteStatus.CONVERTED));

        // WHEN
        assertThrows(IllegalStateException.class,
                () -> orderService.createFromQuote(quoteId));

        // THEN
        // Consultamos directamente H2,
        // fuera de la transacción fallida.
        Long ordersCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE quote_id = ?", Long.class, quoteId);

        String quoteStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM quotes WHERE id = ?", String.class, quoteId);

        assertEquals(0L, ordersCount);
        assertEquals("ACCEPTED", quoteStatus);

        verify(quoteRepository).save(argThat(quote ->
                        quote != null && quote.getStatus() == QuoteStatus.CONVERTED));
    }
}
