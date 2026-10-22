package com.optiflow.crm.quote.domain;

import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.QuoteStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuoteDomainServiceTest {

    private QuoteDomainService service;

    @BeforeEach
    void setUp() {
        service = new QuoteDomainService();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private QuoteItem createItem(
            int quantity,
            String unitPrice,
            String discount,
            String taxRate) {

        QuoteItem item = new QuoteItem();

        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        item.setDiscount(new BigDecimal(discount));
        item.setTaxRate(new BigDecimal(taxRate));

        return item;
    }

    // Comparación numérica sin depender de la escala
    // de BigDecimal (100.0 y 100.00 son equivalentes).
    private void assertMoney(String expected, BigDecimal actual) {

        assertNotNull(actual);

        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "Importe esperado: " + expected + ", obtenido: " + actual);
    }

    // =====================================
    // CALCULATE ITEM
    // =====================================

    @Test
    void givenItem_whenCalculate_thenCalculateSubtotalTaxAndTotal() {

        // GIVEN
        QuoteItem item = createItem(2, "80.00", "10.00", "21.00");

        // WHEN
        service.calculateItem(item);

        // THEN
        // 80 * 2 = 160
        // 160 - 10 = 150
        // IVA 21% = 31.50
        // Total = 181.50

        assertMoney("150.00", item.getSubtotal());
        assertMoney("31.50", item.getTaxAmount());
        assertMoney("181.50", item.getTotal());
    }

    @Test
    void givenDiscountGreaterThanGross_whenCalculate_thenSubtotalIsZero() {

        // GIVEN
        QuoteItem item = createItem(1, "50.00", "80.00", "21.00");

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
        QuoteItem item = createItem(1, "0.50", "0.00", "21.00");

        // WHEN
        service.calculateItem(item);

        // THEN
        // IVA = 0.105 -> 0.11 (HALF_UP)
        assertMoney("0.50", item.getSubtotal());
        assertMoney("0.11", item.getTaxAmount());
        assertMoney("0.61", item.getTotal());
    }

    @Test
    void givenNullMonetaryValues_whenCalculate_thenUseZero() {

        // GIVEN
        QuoteItem item = new QuoteItem();

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
    // CALCULATE TOTALS
    // =====================================

    @Test
    void givenMultipleItems_whenCalculateTotals_thenSumAllAmounts() {

        // GIVEN
        QuoteItem item1 = createItem(2, "80.00", "10.00", "21.00");
        QuoteItem item2 = createItem(1, "100.00", "0.00", "21.00");

        service.calculateItem(item1);
        service.calculateItem(item2);

        Quote quote = new Quote();
        quote.setItems(List.of(item1, item2));

        // WHEN
        service.calculateTotals(quote);

        // THEN
        // Item 1 = 150 + 31.50 = 181.50
        // Item 2 = 100 + 21.00 = 121.00

        assertMoney("250.00", quote.getSubtotal());
        assertMoney("52.50", quote.getTaxAmount());
        assertMoney("302.50", quote.getTotal());
    }

    @Test
    void givenEmptyItems_whenCalculateTotals_thenAllTotalsAreZero() {

        // GIVEN
        Quote quote = new Quote();
        quote.setItems(List.of());

        // WHEN
        service.calculateTotals(quote);

        // THEN
        assertMoney("0.00", quote.getSubtotal());
        assertMoney("0.00", quote.getTaxAmount());
        assertMoney("0.00", quote.getTotal());
    }

    @Test
    void givenNullItemAmounts_whenCalculateTotals_thenIgnoreNulls() {

        // GIVEN
        QuoteItem item1 = new QuoteItem();

        item1.setSubtotal(new BigDecimal("100.00"));
        item1.setTaxAmount(null);
        item1.setTotal(new BigDecimal("121.00"));

        QuoteItem item2 = new QuoteItem();

        item2.setSubtotal(null);
        item2.setTaxAmount(new BigDecimal("21.00"));
        item2.setTotal(null);

        Quote quote = new Quote();
        quote.setItems(List.of(item1, item2));

        // WHEN
        service.calculateTotals(quote);

        // THEN
        assertMoney("100.00", quote.getSubtotal());
        assertMoney("21.00", quote.getTaxAmount());
        assertMoney("121.00", quote.getTotal());
    }

    // =====================================
    // INITIALIZE QUOTE
    // =====================================

    @Test
    void givenEmptyQuote_whenInitialize_thenSetDefaultValues() {

        // GIVEN
        Quote quote = new Quote();

        quote.setQuoteDate(null);
        quote.setExpirationDate(null);
        quote.setTaxRate(null);
        quote.setStatus(null);
        quote.setActive(false);

        LocalDate today = LocalDate.now();

        // WHEN
        service.initializeQuote(quote);

        // THEN
        assertEquals(today, quote.getQuoteDate());
        assertEquals(today.plusDays(30), quote.getExpirationDate());
        assertMoney("21.00", quote.getTaxRate());
        assertEquals(QuoteStatus.DRAFT, quote.getStatus());
        assertTrue(quote.getActive());
    }

    @Test
    void givenExistingValues_whenInitialize_thenPreserveValues() {

        // GIVEN
        Quote quote = new Quote();

        LocalDate quoteDate = LocalDate.of(2026, 10, 1);
        LocalDate expiration = LocalDate.of(2026, 11, 15);

        quote.setQuoteDate(quoteDate);
        quote.setExpirationDate(expiration);
        quote.setTaxRate(new BigDecimal("10.00"));
        quote.setStatus(QuoteStatus.SENT);

        // WHEN
        service.initializeQuote(quote);

        // THEN
        assertEquals(quoteDate, quote.getQuoteDate());
        assertEquals(expiration, quote.getExpirationDate());
        assertMoney("10.00", quote.getTaxRate());
        assertEquals(QuoteStatus.SENT, quote.getStatus());
    }

    @Test
    void givenInactiveQuote_whenInitialize_thenActivateQuote() {

        // GIVEN
        Quote quote = new Quote();
        quote.setActive(false);

        // WHEN
        service.initializeQuote(quote);

        // THEN
        assertTrue(quote.getActive());
    }
}
