package com.optiflow.crm.shared.exception;

public class InventoryItemNotAvailableException extends RuntimeException {

    public InventoryItemNotAvailableException(Long id) {
        super("El artículo de inventario con id " + id + " no está disponible");
    }
}
