package com.optiflow.crm.inventory.model;

import com.optiflow.crm.inventory.dto.InventoryItemDto;
import com.optiflow.crm.inventory.dto.InventorySearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Intefaz de servicios de Inventory Items
public interface InventoryService {

    // Busqueda paginada
    Page<InventoryItem> findAll(Pageable pageable);

    // Busqueda personalizada
    Page<InventoryItem> search(InventorySearchDto searchDto);

    // Busqueda por id
    InventoryItem findById(Long id);

    // Crear uno nuevo
    InventoryItem create(InventoryItemDto dto);

    // Actualizar un item
    InventoryItem update(Long id, InventoryItemDto dto);

    // Borrar un item
    void delete(Long id);

    // Quitar del stock
    void decreaseStock(Long inventoryItemId, Integer quantity);

    // Subir al stock
    void increaseStock(Long inventoryItemId, Integer quantity);
}