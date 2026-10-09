package com.optiflow.crm.domain;

import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class QuoteDomainService {

    public void calculateTotals(Quote quote) {

        BigDecimal subtotal = quote.getSubtotal() != null
                ? quote.getSubtotal() : BigDecimal.ZERO;

        BigDecimal discount = quote.getDiscount() != null
                ? quote.getDiscount() : BigDecimal.ZERO;

        BigDecimal taxRate = quote.getTaxRate() != null
                ? quote.getTaxRate() : new BigDecimal("21.00");

        BigDecimal taxableBase = subtotal.subtract(discount);

        if (taxableBase.compareTo(BigDecimal.ZERO) < 0) {
            taxableBase = BigDecimal.ZERO;
        }

        BigDecimal taxAmount = taxableBase
                .multiply(taxRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        quote.setTaxAmount(taxAmount);
        quote.setTotal(taxableBase.add(taxAmount));
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