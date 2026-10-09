package com.optiflow.crm.domain;

import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.shared.exception.InvalidQuoteStatusException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OrderDomainService {

    public void calculateTotals(Order order) {
        // cálculo monetario
    }

    public void validateQuoteConversion(Quote quote) {

        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new InvalidQuoteStatusException("Solo se pueden convertir presupuestos ACCEPTED");
        }
    }

    public Order createFromQuote(Quote quote) {

        Order order = new Order();

        order.setQuote(quote);
        order.setClient(quote.getClient());

        order.setSubtotal(quote.getSubtotal());
        order.setDiscount(quote.getDiscount());
        order.setTaxRate(quote.getTaxRate());
        order.setTaxAmount(quote.getTaxAmount());
        order.setTotal(quote.getTotal());

        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());
        order.setActive(true);

        return order;
    }
}