package com.optiflow.crm.quote.domain;

import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// Logica de negocio del dominio presupuesto
@Service
public class QuoteDomainService {

    // Calcular los totales del presupuesto
    public void calculateTotals(Quote quote) {

        BigDecimal subtotal = quote.getItems()
                        .stream()
                        .map(QuoteItem::getSubtotal)
                        .filter(value -> value != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = quote.getItems()
                        .stream()
                        .map(QuoteItem::getTaxAmount)
                        .filter(value -> value != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = quote.getItems()
                        .stream()
                        .map(QuoteItem::getTotal)
                        .filter(value -> value != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        quote.setSubtotal(subtotal);
        quote.setTaxAmount(taxAmount);
        quote.setTotal(total);
    }

    // Calcular el precio de los items
    public void calculateItem(QuoteItem item) {

        BigDecimal unitPrice = item.getUnitPrice() != null
                        ? item.getUnitPrice() : BigDecimal.ZERO;

        BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

        BigDecimal discount = item.getDiscount() != null
                        ? item.getDiscount() : BigDecimal.ZERO;

        BigDecimal taxRate = item.getTaxRate() != null
                        ? item.getTaxRate() : BigDecimal.ZERO;

        BigDecimal gross = unitPrice.multiply(quantity);

        BigDecimal subtotal = gross.subtract(discount);

        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            subtotal = BigDecimal.ZERO;
        }

        BigDecimal taxAmount = subtotal.multiply(taxRate).divide(
                                new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.add(taxAmount);

        item.setSubtotal(subtotal);
        item.setTaxAmount(taxAmount);
        item.setTotal(total);
    }

    // Inicializa el presupuesto
    public void initializeQuote(Quote quote) {

        if (quote.getQuoteDate() == null) {
            quote.setQuoteDate(LocalDate.now());
        }

        if (quote.getExpirationDate() == null) {
            quote.setExpirationDate(quote.getQuoteDate().plusDays(30));
        }

        if (quote.getTaxRate() == null) {
            quote.setTaxRate(new BigDecimal("21.00"));
        }

        if (quote.getStatus() == null) {
            quote.setStatus(QuoteStatus.DRAFT);
        }

        quote.setActive(true);
    }
}