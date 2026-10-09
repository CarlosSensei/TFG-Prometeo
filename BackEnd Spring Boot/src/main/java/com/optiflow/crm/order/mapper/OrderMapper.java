package com.optiflow.crm.order.mapper;

import com.optiflow.crm.order.dto.OrderDto;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.quote.model.Quote;

import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {

        OrderDto dto = new OrderDto();

        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());

        if (order.getClient() != null) {
            dto.setClientId(order.getClient().getId());
        }

        if (order.getQuote() != null) {
            dto.setQuoteId(order.getQuote().getId());
        }

        dto.setStatus(order.getStatus());

        dto.setOrderDate(order.getOrderDate());
        dto.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        dto.setDeliveryDate(order.getDeliveryDate());

        dto.setSubtotal(order.getSubtotal());
        dto.setDiscount(order.getDiscount());
        dto.setTaxRate(order.getTaxRate());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setTotal(order.getTotal());

        dto.setNotes(order.getNotes());
        dto.setActive(order.getActive());

        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        return dto;
    }

    public Order toEntity(OrderDto dto) {

        Order order = new Order();

        order.setStatus(dto.getStatus());

        order.setOrderDate(dto.getOrderDate());
        order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        order.setDeliveryDate(dto.getDeliveryDate());

        order.setSubtotal(dto.getSubtotal());
        order.setDiscount(dto.getDiscount());
        order.setTaxRate(dto.getTaxRate());

        order.setNotes(dto.getNotes());

        return order;
    }

    public void updateEntity(OrderDto dto, Order order) {

        order.setStatus(dto.getStatus());

        order.setOrderDate(dto.getOrderDate());
        order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        order.setDeliveryDate(dto.getDeliveryDate());

        order.setSubtotal(dto.getSubtotal());
        order.setDiscount(dto.getDiscount());
        order.setTaxRate(dto.getTaxRate());

        order.setNotes(dto.getNotes());
    }

    public Order fromQuote(Quote quote) {

        Order order = new Order();

        order.setQuote(quote);
        order.setClient(quote.getClient());

        order.setStatus(OrderStatus.PENDING);

        order.setSubtotal(quote.getSubtotal());
        order.setDiscount(quote.getDiscount());
        order.setTaxRate(quote.getTaxRate());
        order.setTaxAmount(quote.getTaxAmount());
        order.setTotal(quote.getTotal());

        order.setNotes(quote.getNotes());

        return order;
    }
}