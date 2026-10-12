package com.optiflow.crm.quote.mapper;

import com.optiflow.crm.quote.dto.QuoteItemDto;
import com.optiflow.crm.quote.model.QuoteItem;
import org.springframework.stereotype.Component;

// Mapper personalizado de los items de presupuesto
@Component
public class QuoteItemMapper {

    public QuoteItemDto toDto(QuoteItem item) {

        QuoteItemDto dto = new QuoteItemDto();

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