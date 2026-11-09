package com.optiflow.crm.order.domain;

import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderItem;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.shared.exception.InvalidOrderStatusException;
import com.optiflow.crm.shared.exception.InvalidQuoteStatusException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// Service con logicas de dominio de pedidos
@Service
public class OrderDomainService {

    // Calculo de totales del pedido a partir de sus items

    public void calculateTotals(Order order) {

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {

            subtotal = subtotal.add(item.getSubtotal() != null
                    ? item.getSubtotal() : BigDecimal.ZERO);
            discount = discount.add(item.getDiscount() != null
                    ? item.getDiscount() : BigDecimal.ZERO);
            taxAmount = taxAmount.add(item.getTaxAmount() != null
                    ? item.getTaxAmount() : BigDecimal.ZERO);
            total = total.add(item.getTotal() != null
                    ? item.getTotal() : BigDecimal.ZERO);
        }

        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTaxAmount(taxAmount);
        order.setTotal(total);
    }


    public void calculateItem(OrderItem item) {

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

    // Validar presupuestos
    public void validateQuoteConversion(Quote quote) {

        if (!Boolean.TRUE.equals(quote.getActive())) {
            throw new InvalidQuoteStatusException(
                    "No se puede convertir un presupuesto inactivo"
            );
        }

        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new InvalidQuoteStatusException(
                    "Solo se pueden convertir presupuestos aceptados"
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
            orderItem.setTaxAmount(quoteItem.getTaxAmount());
            orderItem.setTotal(quoteItem.getTotal());

            order.getItems().add(orderItem);
        }

        calculateTotals(order);

        return order;
    }


    public void validateCanEdit(Order order) {

        validateActive(order);

        requireStatus(order, OrderStatus.PENDING);
    }

    public void validateCanDelete(Order order) {

        validateCanEdit(order);
    }

    public void validateCanConfirm(Order order) {

        validateActive(order);

        requireStatus(order, OrderStatus.PENDING);

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new InvalidOrderStatusException
                    ("No se puede confirmar un pedido sin artículos");
        }
    }

    public void startPreparation(Order order) {

        validateActive(order);
        requireStatus(order, OrderStatus.CONFIRMED);
        order.setStatus(OrderStatus.IN_PROGRESS);
    }

    public void markReady(Order order) {

        validateActive(order);
        requireStatus(order, OrderStatus.IN_PROGRESS);
        order.setStatus(OrderStatus.READY);
    }

    public void deliver(Order order) {

        validateActive(order);
        requireStatus(order, OrderStatus.READY);
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveryDate(LocalDate.now());
    }

    public void validateCanCancel(Order order) {

        validateActive(order);

        if (order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.DELIVERED) {

            throw new InvalidOrderStatusException(
                    "El pedido no se puede cancelar en estado " +
                            order.getStatus()
            );
        }
    }

    private void validateActive(Order order) {

        if (!Boolean.TRUE.equals(order.getActive())) {

            throw new InvalidOrderStatusException(
                    "El pedido está inactivo"
            );
        }
    }

    private void requireStatus(Order order, OrderStatus required) {

        if (order.getStatus() != required) {

            throw new InvalidOrderStatusException(
                    "Se requiere el estado " + required +
                            ". Estado actual: " + order.getStatus()
            );
        }
    }



}