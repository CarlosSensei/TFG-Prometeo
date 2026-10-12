package com.optiflow.crm.order.mapper;

import com.optiflow.crm.order.dto.OrderItemDto;
import com.optiflow.crm.order.model.OrderItem;
import org.springframework.stereotype.Component;

// Mapper de items del pedido
@Component
public class OrderItemMapper {

    // Item a Dto
    public OrderItemDto toDto(OrderItem item) {

        OrderItemDto dto = new OrderItemDto();

        dto.setId(item.getId());

        if (item.getInventoryItem() != null) {
            dto.setInventoryItemId(item.getInventoryItem().getId());
        }

        dto.setQuantity(item.getQuantity());
        dto.setDescription(item.getDescription());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscount(item.getDiscount());
        dto.setTaxRate(item.getTaxRate());
        dto.setSubtotal(item.getSubtotal());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setTotal(item.getTotal());

        return dto;
    }
}