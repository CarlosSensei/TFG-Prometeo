package com.optiflow.crm.inventory.mapper;

import com.optiflow.crm.inventory.dto.InventoryItemDto;
import com.optiflow.crm.inventory.model.InventoryItem;
import org.springframework.stereotype.Component;

@Component
public class InventoryItemMapper {

    public InventoryItemDto toDto(InventoryItem item) {

        InventoryItemDto dto = new InventoryItemDto();

        dto.setId(item.getId());
        dto.setSku(item.getSku());
        dto.setName(item.getName());
        dto.setType(item.getType());
        dto.setBrand(item.getBrand());
        dto.setDescription(item.getDescription());
        dto.setSalePrice(item.getSalePrice());
        dto.setTaxRate(item.getTaxRate());
        dto.setStock(item.getStock());
        dto.setActive(item.getActive());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());

        return dto;
    }

    public InventoryItem toEntity(InventoryItemDto dto) {

        InventoryItem item = new InventoryItem();

        item.setSku(dto.getSku());
        item.setName(dto.getName());
        item.setType(dto.getType());
        item.setBrand(dto.getBrand());
        item.setDescription(dto.getDescription());
        item.setSalePrice(dto.getSalePrice());
        item.setTaxRate(dto.getTaxRate());
        item.setStock(dto.getStock());

        return item;
    }

    public void updateEntity(InventoryItemDto dto, InventoryItem item) {

        item.setSku(dto.getSku());
        item.setName(dto.getName());
        item.setType(dto.getType());
        item.setBrand(dto.getBrand());
        item.setDescription(dto.getDescription());
        item.setSalePrice(dto.getSalePrice());
        item.setTaxRate(dto.getTaxRate());
        item.setStock(dto.getStock());
    }
}