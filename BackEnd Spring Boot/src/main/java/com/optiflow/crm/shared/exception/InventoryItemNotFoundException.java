package com.optiflow.crm.shared.exception;

public class InventoryItemNotFoundException extends ResourceNotFoundException {

    public InventoryItemNotFoundException(Long id) {
        super("Artículo de inventario no encontrado con id: " + id);
    }
}