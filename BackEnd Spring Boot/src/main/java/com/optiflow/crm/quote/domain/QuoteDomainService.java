package com.optiflow.crm.quote.domain;

import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class QuoteDomainService {

    public void calculateTotals(Quote quote) {

        BigDecimal subtotal = quote.getItems()
                        .stream()
                        .map(QuoteItem::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = quote.getItems()
                        .stream()
                        .map(QuoteItem::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        quote.setSubtotal(subtotal);
        quote.setTotal(total);
    }

    public void calculateItem(QuoteItem item) {

        BigDecimal unitPrice = item.getUnitPrice();
        BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

        BigDecimal discount = item.getDiscount() != null
                        ? item.getDiscount() : BigDecimal.ZERO;

        BigDecimal base = unitPrice.multiply(quantity).subtract(discount);

        if (base.compareTo(BigDecimal.ZERO) < 0) {
            base = BigDecimal.ZERO;
        }

        BigDecimal taxAmount = base.multiply(item.getTaxRate())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        item.setSubtotal(base);
        item.setTotal(base.add(taxAmount));
    }

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