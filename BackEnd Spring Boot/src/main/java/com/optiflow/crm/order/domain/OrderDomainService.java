package com.optiflow.crm.order.domain;

import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderItem;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.shared.exception.InvalidQuoteStatusException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class OrderDomainService {

    // Calculo de totales del pedido a partir de sus items
    public void calculateTotals(Order order) {

        BigDecimal subtotal = order.getItems()
                .stream()
                .map(OrderItem::getSubtotal)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = order.getItems()
                .stream()
                .map(OrderItem::getTotal)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount =
                total.subtract(subtotal);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setTotal(total);
    }

    // Validar presupuestos
    public void validateQuoteConversion(Quote quote) {

        if (quote.getStatus() != QuoteStatus.ACCEPTED) {

            throw new InvalidQuoteStatusException(
                    "Solo se pueden convertir presupuestos ACCEPTED"
            );
        }
    }

    // Metodo para inicializar presupuestos
    public void initializeOrder(Order order) {

        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }

        if (order.getTaxRate() == null) {
            order.setTaxRate(new BigDecimal("21.00"));
        }

        if (order.getActive() == null) {
            order.setActive(true);
        }
    }

    // Crear un pedido desde un presupuesto
    public Order createFromQuote(Quote quote) {

        Order order = new Order();

        order.setQuote(quote);
        order.setClient(quote.getClient());
        order.setSeller(quote.getSeller());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());
        order.setActive(true);

        // Copiamos los items del presupuesto al pedido
        for (QuoteItem quoteItem : quote.getItems()) {

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setInventoryItem(quoteItem.getInventoryItem());
            orderItem.setDescription(quoteItem.getDescription());
            orderItem.setQuantity(quoteItem.getQuantity());
            orderItem.setUnitPrice(quoteItem.getUnitPrice());
            orderItem.setDiscount(quoteItem.getDiscount());
            orderItem.setTaxRate(quoteItem.getTaxRate());
            orderItem.setSubtotal(quoteItem.getSubtotal());
            orderItem.setTotal(quoteItem.getTotal());

            order.getItems().add(orderItem);
        }

        calculateTotals(order);

        return order;
    }
}